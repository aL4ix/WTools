import pathlib
from configparser import ConfigParser
from pathlib import Path

from pandas import DataFrame

CONFIG_HEADER = 'DEFAULT'

previous_file = ''


def filter_and_report(file: str, one_line: str, results: list):
    global previous_file
    file = file.removesuffix('.java')
    if file != previous_file:
        results.append((file, ''))
        previous_file = file
    one_line = one_line.split('public')[1]
    one_line = one_line.replace('()', '').replace('void', '').replace('{', '')
    one_line = one_line.strip()
    results.append(('', one_line))
    print(file)
    print(one_line)


def analyze_testng_groups():
    config = ConfigParser()
    config.read('configuration.ini')
    section = config[CONFIG_HEADER]
    repo_path = section['repo_path']
    path_inside = section['path_inside']
    split = section['skips'].split(',')
    skips = [x.strip() for x in split]

    results = []
    count = 0
    ori_root: Path = pathlib.Path(repo_path + path_inside)
    for root, dirs, files in ori_root.walk():
        for file in files:
            if file.lower().endswith('.java'):
                with open(root / file, encoding='utf-8') as f:
                    is_test_lines = False
                    gathered_lines = []
                    for line in f.readlines():
                        if '@Test' in line:
                            is_test_lines = True
                        if is_test_lines:
                            if 'public' in line:
                                is_test_lines = False
                                gathered_lines.append(line.strip())
                                one_line = '\n'.join(gathered_lines)
                                # Filter
                                if one_line.count('"a11y"') >= 1:
                                    # print(one_line.count('"another_group"'))
                                    filter_and_report(file, one_line, results)
                                    count += 1

                                gathered_lines = []
                                continue
                            else:
                                for skip in skips:
                                    if skip in line:
                                        break
                                else:
                                    gathered_lines.append(line.strip())
    print(count)
    df = DataFrame(results)
    print(df)
    df.to_csv('report.csv')


if __name__ == '__main__':
    analyze_testng_groups()
