import codecs
import configparser
import csv
import inspect
import json
import os
import re
import subprocess
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from typing import Dict, List

import pandas as pd

CONFIGURATION_INI = 'configuration.ini'
DEFAULT_SECTION = 'DEFAULT'
TEST_RUN_ID_COLUMN = 'testRunId'


@dataclass
class JunitResults:
    test_path: str
    test_name: str
    status: bool
    failure_reason: str
    response_body: str


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
    print(inspect.currentframe().f_code.co_name+'()')
    default_section = get_config_default_section()
    url = default_section['url']
    key = default_section['key']
    cmd = f'curl --output collection.postman_collection.json {url}?access_key={key}'
    print(cmd)
    if user_confirmation() == 'y':
        subprocess.check_call(cmd, shell=True)
    return url


def user_confirmation():
    r = input('RUN? [yn] ').lower()
    return r


def runner_with_filter():
    url = fetch()
    with codecs.open('collection.json', encoding='utf-8') as f:
        text = f.read()
        collection = json.loads(text)
        o = find_requests(collection['collection']['item'])
        cmd_lines = []
        start_cmd = f'newman run {url} -e QA.postman_environment.json -r junit'
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
            for _ in range(3):
                rc = subprocess.call(c, shell=True)
                print(rc)
                if rc == 0:
                    break
        subprocess.check_call('npx jrm combined.xml newman/*.xml', shell=True)


def parse_junit(junit_file) -> list[JunitResults]:
    tree = ET.parse(junit_file)
    root = tree.getroot()

    tests = []

    for testsuite in root.iter('testsuite'):
        test_path = testsuite.attrib.get('name')

        for testcase in testsuite.iter('testcase'):
            test_name = testcase.attrib.get('name')
            status = True
            failure_reason = ''

            failure = testcase.find('failure')
            error = testcase.find('error')

            if failure is not None or error is not None:
                status = False
                failure_reason = failure.text.strip()

            tests.append(JunitResults(test_path, test_name, status, failure_reason, ''))

    return tests


def create_csv_report(tests: list[JunitResults], output_file):
    with open(output_file, mode='w', newline='', encoding='utf8') as file:
        writer = csv.writer(file)
        writer.writerow(['Test Path', 'Test Case', 'Status', 'Failure Reason', 'Response Body'])

        for test in tests:
            test_path = test.test_path
            test_name = test.test_name
            status = test.status
            failure_reason = test.failure_reason
            body = test.response_body
            writer.writerow([test_path, test_name, status, failure_reason, body])


def runner(environment: str, folders: list[str] = None):
    print(inspect.currentframe().f_code.co_name+'()')
    if folders:
        folder_param = '--folder'
        folder_params = ' '.join([f'{folder_param} "{folder}"' for folder in folders])
    else:
        folder_params = ''
    cmd = (f'newman run collection.postman_collection.json {folder_params} '
           f'-e {environment}.postman_environment.json -r junit,csv,cli --reporter-csv-includeBody '
           f'--reporter-junit-export newman/newman-run-report.xml '
           f'--reporter-csv-export newman/newman-run-report.csv ')
    print(cmd)
    uc = user_confirmation()
    if uc == 'y':
        subprocess.call(cmd, shell=True)


def run_and_report_to_testrail(environment: str, project_id: int, suite_id: int, run_id: int, folders: list[str] = None,
                               ddt: str = None,
                               user_input=''):
    print(inspect.currentframe().f_code.co_name+'()')
    default_section = get_config_default_section()
    os.environ['TESTRAIL_DOMAIN'] = default_section['TESTRAIL_DOMAIN']
    os.environ['TESTRAIL_USERNAME'] = default_section['TESTRAIL_USERNAME']
    os.environ['TESTRAIL_APIKEY'] = default_section['TESTRAIL_APIKEY']
    os.environ['TESTRAIL_PROJECTID'] = str(project_id)
    os.environ['TESTRAIL_SUITEID'] = str(suite_id)
    if run_id:
        os.environ['TESTRAIL_RUNID'] = str(run_id)
    os.environ['TESTRAIL_LOGGING'] = 'full'
    os.environ['TESTRAIL_INCLUDEALL'] = 'false'

    if folders:
        folder_param = '--folder'
        folder_params = ' '.join([f'{folder_param} "{folder}"' for folder in folders])
    else:
        folder_params = ''

    if ddt:
        ddt_param = f'-d {ddt}'
    else:
        ddt_param = ''

    subprocess.check_call('set', shell=True)
    cmd = (f'newman run collection.postman_collection.json -e {environment}.postman_environment.json -r testrail,cli '
           f'{folder_params} {ddt_param}')
    print(cmd)
    if user_input != 'a':
        user_input = input('RUN? [yna] ').lower()
    if user_input in ('y', 'a'):
        subprocess.check_call(cmd, shell=True)
    return user_input


def ddt_with_a_test_run_per_line(environment: str, project_id: int, suite_id: int, folders: list[str] = None):
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

            print(open(file_name, encoding='utf-8').read())
            user_input = run_and_report_to_testrail(environment, project_id, suite_id, test_run_id, folders, file_name,
                                                    user_input)


def find_request_item(data):
    if 'item' in data:
        for item in data['item']:
            yield from find_request_item(item)
    else:
        yield data


def extract_request_and_test_names(file_path: str) -> list[tuple]:
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    folders = data['collection']['item']

    requests_and_test_names = []

    for folder in folders:
        for request_item in find_request_item(folder):

            if request_item:
                request_name = request_item['name']
                for event in request_item.get('event', []):
                    if event['listen'] == 'test':
                        script = '\n'.join(event['script']['exec'])
                        for match in re.finditer(r'pm\.test\("(.*)"\s*,', script):
                            test_name = match.group(1)
                            requests_and_test_names.append((request_name, test_name))
                        break
    return requests_and_test_names


def extract_unique_test_ids(requests_and_test_names: list[tuple]) -> dict:
    test_ids = {}
    for request, test_name in requests_and_test_names:
        match = re.search(r'^C(\d+)', test_name)
        if match:
            test_ids[match.group(1)] = request
    return test_ids


def add_body_to_failed_tests_from_csv_report(tests: list[JunitResults], csv_report: str, add_body_to_passed: bool):
    df = pd.read_csv(csv_report, keep_default_na=False)

    for test in tests:
        if add_body_to_passed or test.status == False:
            request_name = test.test_path.split(' / ')[-1]
            name_ = df.loc[df['requestName'] == request_name]
            body_df = name_['body']
            body = body_df.values[0]
            test.response_body = body


def jenkins_run(environment: str, project_id: int, suite_id: int):
    print(inspect.currentframe().f_code.co_name+'()')
    default_section = get_config_default_section()
    os.environ['TESTRAIL_DOMAIN'] = default_section['TESTRAIL_DOMAIN']
    os.environ['TESTRAIL_USERNAME'] = default_section['TESTRAIL_USERNAME']
    os.environ['TESTRAIL_APIKEY'] = default_section['TESTRAIL_APIKEY']
    os.environ['TESTRAIL_PROJECTID'] = str(project_id)
    os.environ['TESTRAIL_SUITEID'] = str(suite_id)
    os.environ['TESTRAIL_LOGGING'] = 'full'
    # os.environ['TESTRAIL_INCLUDEALL'] = 'false'

    subprocess.check_call('set', shell=True)
    cmd = f'newman run collection.postman_collection.json -e {environment}.postman_environment.json -r cli,junit,allure,testrail --reporter-junit-export newman/Learning_Destinations_ms_userQA_junit.xml --env-var Suite=Regression'
    print(cmd)
    subprocess.check_call(cmd, shell=True)


def main():
    default_section = get_config_default_section()
    folders = default_section['folders']
    project_id = default_section['project_id']
    suite_id = default_section['suite_id']
    run_id = default_section['run_id']
    environment = default_section['environment']

    # fetch()
    # runner(environment)
    # tests = parse_junit('newman/newman-run-report.xml')
    # add_body_to_failed_tests_from_csv_report(tests, 'newman/newman-run-report.csv', False)
    # create_csv_report(tests, 'report.csv')

    # run_and_report_to_testrail(environment, project_id, suite_id, run_id, folders)
    # ddt_with_a_test_run_per_line(environment, project_id, suite_id, folders)

    # requests_and_test_names = extract_request_and_test_names('collection.postman_collection.json')
    # test_ids = extract_unique_test_ids(requests_and_test_names)
    # df = pd.DataFrame.from_dict(test_ids, orient='index', columns=['source'])
    # df.index.name = 'id'
    # print(df)
    # df.to_csv('report_ids.csv')

    # jenkins_run(environment, project_id, suite_id)
    print('Finished')


if __name__ == '__main__':
    main()
    print('\a')
