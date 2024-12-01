import os

import pandas as pd


class TestrailFacade:
    def __init__(self, testrail_client):
        self.client = testrail_client

    def update_run(self, body: dict, test_run_id: int) -> dict:
        return self.client.send_post(f'update_run/{test_run_id}', body)

    def get_tests_from_run(self, test_run_id: int, query: str) -> dict:
        result = {
            'tests': []
        }
        one = self.client.send_get(f'get_tests/{test_run_id}{query}')
        while True:
            result['tests'].extend(one['tests'])
            links = one['_links']
            next_ = links['next']
            print(repr(links))
            if not next_:
                break
            one = self.client.send_get(next_[8:])
        return result

    def get_statuses(self) -> dict:
        return self.client.send_get('get_statuses')

    @staticmethod
    def append_case_ids_from_run_tests(dest: list, tests: dict) -> None:
        for test in tests['tests']:
            dest.append(test['case_id'])

    def get_test(self, test_id: int) -> dict:
        return self.client.send_get(f'get_test/{test_id}')

    def get_case(self, case_id: int) -> dict:
        return self.client.send_get(f'get_case/{case_id}')

    def get_plan(self, plan_id: int) -> dict:
        return self.client.send_get(f'get_plan/{plan_id}')

    def report_test_cases(self, project_ids_suite_ids: list[tuple[int]], filename_prefix: str, folder: str):
        """

        :param project_ids_suite_ids: List of pairs containing project and suite.
        like: [(12, 123), (12, 124)]
        :param filename_prefix:
        :param folder:
        :return:
        """
        for project_id, suite_id in project_ids_suite_ids:
            tcs = self.client.send_get(
                f'get_cases/{project_id}&suite_id={suite_id}')
            df = pd.DataFrame(tcs)
            outfile = f'{filename_prefix}_{project_id}_{suite_id}.csv'
            df.to_csv(os.path.join(folder, outfile))
