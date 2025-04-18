import configparser
import os

import pandas as pd

from testrail import APIClient, APIError
from testrail_facade import TestrailFacade

DEFAULT_SECTION = 'DEFAULT'


def remove_a11y_and_mobile_for_test_run(facade: TestrailFacade, test_run_id: int) -> None:
    all_tests = facade.get_tests_from_run(test_run_id, '')

    all_ids = []
    ids_to_remove = []

    facade.append_case_ids_from_run_tests(all_ids, all_tests)

    for test in all_tests['tests']:
        title = test['title'].lower()
        forbidden_titles = ('accessibility', 'a11y', 'mobile')
        for forbidden_title in forbidden_titles:
            if forbidden_title in title:
                ids_to_remove.append(test['case_id'])

    result_ids = list(set(all_ids) - set(ids_to_remove))

    print(f'all={len(all_ids)} remove={len(ids_to_remove)} result={len(result_ids)}')

    input('PROCEED?')

    body = {
        'include_all': False,
        'case_ids': result_ids
    }
    print(body)
    ret = facade.update_run(body, test_run_id)
    print(repr(ret))

    print('Finished')


def remove_untested_from_test_run(facade: TestrailFacade, test_run_id: int) -> None:
    all_tests = facade.get_tests_from_run(test_run_id, '')
    untested_tests = facade.get_tests_from_run(test_run_id, '&status_id=3')

    all_ids = []
    ids_to_remove = []

    facade.append_case_ids_from_run_tests(all_ids, all_tests)
    facade.append_case_ids_from_run_tests(ids_to_remove, untested_tests)

    result_ids = list(set(all_ids) - set(ids_to_remove))

    print(f'all={len(all_ids)} remove={len(ids_to_remove)} result={len(result_ids)}')

    input('PROCEED?')

    body = {
        'include_all': False,
        'case_ids': result_ids
    }
    print(body)
    ret = facade.update_run(body, test_run_id)
    print(repr(ret))

    print('Finished')


def save_repr_result(result) -> None:
    with open('result.txt', 'w', encoding='utf8') as f:
        f.write(repr(result))
    print('Finished')


def get_list_of_case_ids_from_tests(facade: TestrailFacade, test_urls_in_a_str_with_enters: str) -> list[int]:
    """

    :param facade:
    :param test_urls_in_a_str_with_enters: like: \"""https://url.to.testrail.io/index.php?/tests/view/12345678
https://url.to.testrail.io/index.php?/tests/view/12345679
\"""
    :return:
    """
    test_ids = [int(test[test.rindex('/'):]) for test in test_urls_in_a_str_with_enters.splitlines()]
    case_ids = []

    for test_id in test_ids:
        ret = facade.get_test(test_id)
        case_ids.append(ret['case_id'])
    print(repr(case_ids))
    return case_ids


def add_cases_to_run(facade: TestrailFacade, test_run_id: int, ids_to_add: list[int]):
    """

    :param facade:
    :param test_run_id:
    :param ids_to_add: like: [1234567, 1234568]
    :return:
    """

    all_tests = facade.get_tests_from_run(test_run_id, '')
    ids = []
    facade.append_case_ids_from_run_tests(ids, all_tests)
    ids.extend(ids_to_add)

    body = {
        'include_all': False,
        'case_ids': ids
    }
    print(body)
    input('PROCEED?')
    ret = facade.update_run(body, test_run_id)
    print(repr(ret))
    print('Finished')


def get_tcs_by_ref(tcs_files: list[str], ref: str, input_folder: str):
    df = pd.DataFrame()
    for tcs_file in tcs_files:
        filepath = str(os.path.join(input_folder, tcs_file))
        df_ref = pd.read_csv(filepath, dtype=str, na_filter=False)
        df = df.append(df_ref.loc[df_ref['refs'].str.contains(ref)])
    return df


def report_tcs_by_refs(tcs_files: list[str], refs: list[str], input_folder: str, filename_prefix: str,
                       output_folder: str):
    dfs = []
    for ref in refs:
        dfs.append(get_tcs_by_ref(tcs_files, ref, input_folder))
    refs_as_string = '_'.join(refs)
    outfile = f'{filename_prefix}__{refs_as_string}.csv'
    df = pd.concat(dfs, sort=False)
    df.to_csv(os.path.join(output_folder, outfile))


def watch_for_postman_test_ids_and_get_refs(facade: TestrailFacade, jira_host: str, testrail_host: str):
    import re
    import pyperclip
    import time
    import webbrowser
    print('Watching for postman test ids')
    while True:
        clipboard = pyperclip.paste()
        if re.match(r'C\d+$', clipboard):
            clipboard = clipboard.removeprefix('C')
            print(clipboard)
            pyperclip.copy('')
            res = facade.get_case(clipboard)
            refs = res['refs']
            print(refs)
            if refs is None:
                url = f'{testrail_host}index.php?/cases/view/{clipboard}'
            else:
                pyperclip.copy(refs)
                url = f'https://{jira_host}.atlassian.net/browse/{refs}'
            webbrowser.open(url)
        time.sleep(1)


def get_test_runs_ids_and_names_from_test_plan(facade: TestrailFacade, test_plan_id:id):
    result = []
    response = facade.get_plan(test_plan_id)
    for entry in response['entries']:
        result.append((entry['runs'][0]['id'], entry['name']))
    return result


def report_list_of_tuples_to_csv(list_of_tuples: list[tuple], csv_file_name: str, columns):
    df = pd.DataFrame(list_of_tuples, columns=columns)
    df.to_csv(csv_file_name, index=False)


def check_case_ids_exist(facade: TestrailFacade, test_ids_and_source: list[tuple]):
    for case_id, source in test_ids_and_source:
        try:
            facade.get_case(case_id)
        except APIError as e:
            print(f'Failed to get case_id "{case_id}" with source "{source}"\n{e}')


def merge_test_runs(facade: TestrailFacade, test_run_ids: list[int]):
    merged = []
    statuses = facade.get_statuses()
    for run_id in test_run_ids:
        tests = facade.get_tests_from_run(run_id, '')
        for test in tests['tests']:
            title = test['title']
            case_id = test['case_id']
            refs = test['refs']
            status_id = test['status_id']
            # Search the status id within the statuses
            status = next((s['label'] for s in statuses if s['id'] == status_id))
            row = (title, case_id, refs, status)
            merged.append(row)
    report_list_of_tuples_to_csv(merged, 'merged_test_runs.csv', ('Title', 'Case id', 'Refs', 'Status'))


def main():
    config = configparser.ConfigParser()
    config.read('configuration.ini')
    default_section = config[DEFAULT_SECTION]
    host = default_section['host']
    jira_host = default_section['jira_host']
    client = APIClient(host)
    client.user = default_section['username']
    client.password = default_section['api_key']
    facade = TestrailFacade(client)

    # remove_a11y_and_mobile_for_test_run(facade, test_run_id)
    # remove_untested_from_test_run(facade, test_run_id)
    # ids_to_add = get_list_of_cases_from_test(facade, test_urls_in_a_str_with_enters)
    # add_cases_to_run(facade, test_run_id, ids_to_add)
    # watch_for_postman_test_ids_and_get_refs(facade, jira_host, host)
    # r = get_test_runs_ids_and_names_from_test_plan(facade, test_plan_id)
    # report_list_of_tuples_to_csv(r, 'report.csv')
    # check_case_ids_exist(facade, [])


if __name__ == '__main__':
    main()
