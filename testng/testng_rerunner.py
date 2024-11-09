#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import codecs
import os
import re
import subprocess
import xml.etree.ElementTree as EleTree
from configparser import ConfigParser
from pathlib import Path


def main():
    print('Starting testng re-runner')
    configparser = ConfigParser()
    configparser.read('configuration.ini')
    default_section = configparser['DEFAULT']
    mvn_cmd = default_section['mvn_cmd'].replace('\n', ' ')
    results = default_section['results']
    xml_file_pattern = default_section['xml_file_pattern']

    pattern = re.compile('{(\\d+)}')
    all_params = pattern.findall(mvn_cmd)
    last_param = int(sorted(all_params)[-1]) + 1

    parser = argparse.ArgumentParser(description='Testng re-runner')
    parser.add_argument("mvn_params", nargs="+")
    args = parser.parse_args()
    mvn_params = args.mvn_params
    mvn_params_count = len(mvn_params)
    print(f'Needed number of parameters {last_param}, Passed {mvn_params_count}.')
    if mvn_params_count != last_param:
        exit(1)

    for env in os.listdir(results):
        xml_file = xml_file_pattern.format(**locals())
        print(xml_file)
        if not Path(xml_file).is_file():
            print('FILE DOES NOT EXIST!')
            break
        tree = EleTree.parse(xml_file)
        root = tree.getroot()
        suite = root.find('suite')
        test_node = suite.find('test')
        list_of_failures = []

        for child in test_node:
            if child.tag == 'class':
                class_name = child.attrib['name']
                for sub_child in child:
                    if sub_child.tag == 'test-method' and ('is-config' not in sub_child.attrib):
                        name = sub_child.attrib['name']
                        status = sub_child.attrib['status']
                        test_path = f'{class_name}#{name}'
                        if status in ['FAIL', 'SKIP']:
                            print(f'{test_path} {status}')
                            if test_path not in list_of_failures:
                                list_of_failures.append(test_path)
                        if status in ['PASS']:
                            if test_path in list_of_failures:
                                print(f'{test_path} CHANGING TO PASS')
                                list_of_failures.remove(test_path)

        if list_of_failures:
            all_inline = ','.join(list_of_failures)
            mvn = mvn_cmd.format(*mvn_params)
            cmd = f'{mvn} -Dtest={all_inline}'
            print(cmd)
            subprocess.call(cmd, shell=True)
        else:
            print('No failures')

    allure_results = 'allure-results'
    for allure in os.listdir(allure_results):
        if allure.lower().endswith('.json'):
            with codecs.open(f'{allure_results}/{allure}', encoding='utf-8') as f:
                contents = f.read().replace('Surefire suite', 'Reruns').replace('Surefire test', 'Reruns')

            with codecs.open(f'{allure_results}/{allure}', encoding='utf-8', mode='w') as f:
                f.write(contents)
    print('DONE')


if __name__ == '__main__':
    main()
