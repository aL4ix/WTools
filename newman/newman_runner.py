import codecs
import configparser
import csv
import json
import os
import subprocess
import xml.etree.ElementTree as ET
from typing import Dict, List

CONFIGURATION_INI = 'configuration.ini'
DEFAULT_SECTION = 'DEFAULT'
TEST_RUN_ID_COLUMN = 'testRunId'


def find_requests(item_list: List, output=None) -> List:
    if output is None:
        output = []
    item: Dict
    for item in item_list:
        if 'item' in item.keys():
            output.extend(find_requests(item['item']))
        else:
            if 'request' in item.keys():
                name = item['name']
                req = item['request']
                if 'description' in req.keys():
                    if '+' in req['description']:  # This is the important part
                        output.append(name)
    return output


def get_config_default_section():
    config = configparser.ConfigParser()
    config.read(CONFIGURATION_INI)
    default_section = config[DEFAULT_SECTION]
    return default_section


def fetch():
    default_section = get_config_default_section()
    url = default_section['url']
    key = default_section['key']
    fetch_collection = default_section['fetch_collection']
    if fetch_collection:
        cmd = f'curl --output collection.postman_collection.json {url}?access_key={key}'
        print(cmd)
        r = input('RUN? [yn] ').lower()
        if r == 'y':
            subprocess.check_call(cmd, shell=True)
    return url


def runner_with_filter():
    url = fetch()
    with codecs.open('collection.json', encoding='utf-8') as f:
        text = f.read()
        collection = json.loads(text)
        o = find_requests(collection['collection']['item'])
        cmd_lines = []
        start_cmd = f'npx newman run {url} -e QA.postman_environment.json -r junit'
        cmd_line = start_cmd
        for t in o:
            if len(cmd_line) > 8000:
                cmd_lines.append(cmd_line)
                cmd_line = start_cmd
            cmd_line += f' --folder "{t}"'
        if len(cmd_line) > 0:
            cmd_lines.append(cmd_line)
        print(cmd_lines)
        # for line in cmd_lines:
        #     print(len(line))
        #     print(line)
        #     subprocess.check_call(line, shell=True)
        for t in o:
            c = f'{start_cmd} --folder "{t}"'
            print(c)
            for retry in range(3):
                rc = subprocess.call(c, shell=True)
                print(rc)
                break
                if rc == 0:
                    break
        subprocess.check_call('npx jrm combined.xml newman/*.xml', shell=True)


def parse_junit(junit_file):
    tree = ET.parse(junit_file)
    root = tree.getroot()

    passed_tests = []
    failed_tests = []

    for testsuite in root.iter('testsuite'):
        suite_name = testsuite.attrib.get('name')

        for testcase in testsuite.iter('testcase'):
            test_name = testcase.attrib.get('name')
            result = 'passed'
            failure_reason = ''

            failure = testcase.find('failure')
            error = testcase.find('error')

            if failure is not None:
                result = 'failed'
                failure_reason = failure.text.strip()
            elif error is not None:
                result = 'failed'
                failure_reason = error.text.strip()

            if result == 'passed':
                passed_tests.append((suite_name, test_name))
            else:
                failed_tests.append((suite_name, test_name, failure_reason))

    return passed_tests, failed_tests


def create_csv_report(passed_tests, failed_tests, output_file):
    with open(output_file, mode='w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(['Test Suite', 'Test Case', 'Result', 'Failure Reason'])

        for class_name, test_name in passed_tests:
            writer.writerow([class_name, test_name, 'passed', ''])

        for class_name, test_name, failure_reason in failed_tests:
            writer.writerow([class_name, test_name, 'failed', failure_reason])


def runner():
    cmd = 'npx newman run collection.postman_collection.json ' \
          '-e QA.postman_environment.json -r junit,progress,csv --reporter-csv-includeBody'
    subprocess.check_call(cmd, shell=True)


def run_and_report_to_testrail(project_id: int, suite_id: int, run_id: int, folders: list[str] = None, ddt: str = None,
                               user_input=''):
    default_section = get_config_default_section()
    os.environ['TESTRAIL_DOMAIN'] = default_section['TESTRAIL_DOMAIN']
    os.environ['TESTRAIL_USERNAME'] = default_section['TESTRAIL_USERNAME']
    os.environ['TESTRAIL_APIKEY'] = default_section['TESTRAIL_APIKEY']
    os.environ['TESTRAIL_PROJECTID'] = str(project_id)
    os.environ['TESTRAIL_SUITEID'] = str(suite_id)
    os.environ['TESTRAIL_RUNID'] = str(run_id)

    if folders:
        folder_param = '--folder'
        folder_params = ' '.join([f'{folder_param} "{folder}"' for folder in folders])
    else:
        folder_params = ''

    if ddt:
        ddt_param = f'-d {ddt}'
    else:
        ddt_param = ''

    cmd = (f'newman run collection.postman_collection.json -e QA.postman_environment.json -r testrail,cli '
           f'{folder_params} {ddt_param}')
    print(cmd)
    if user_input != 'a':
        user_input = input('RUN? [yna] ').lower()
    if user_input in ('y', 'a'):
        subprocess.call(cmd, shell=True)
    return user_input


def ddt_with_a_test_run_per_line(project_id: int, suite_id: int, folders: list[str] = None):
    user_input = ''
    with open('DDT_one_test_run_per_line.csv', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        fieldnames = reader.fieldnames
        for row in reader:
            test_run_id = row[TEST_RUN_ID_COLUMN]
            file_name = 'DDT.csv'

            with open(file_name, mode='w', newline='', encoding='utf-8') as new_csvfile:
                writer = csv.DictWriter(new_csvfile, fieldnames=fieldnames)
                writer.writeheader()
                writer.writerow(row)

            print(open(file_name).read())
            user_input = run_and_report_to_testrail(project_id, suite_id, test_run_id, folders, file_name, user_input)


def main():
    # runner()
    # passed, failed = parse_junit('newman/newman-run-report.xml')
    # create_csv_report(passed, failed, 'report.csv')
    fetch()
    folders = ['Folder one', 'Folder two']
    project_id = 12
    suite_id = 12345
    # run_and_report_to_testrail(project_id, suite_id, 23456, folders)
    ddt_with_a_test_run_per_line(project_id, suite_id, folders)


if __name__ == '__main__':
    main()
