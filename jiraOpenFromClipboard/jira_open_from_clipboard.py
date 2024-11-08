#!/usr/bin/env python3

import re
import time
import webbrowser
from configparser import ConfigParser

import pyperclip

CONFIG_HEADER = 'DEFAULT'


def main():
    config = ConfigParser()
    config.read('configuration.ini')
    section = config[CONFIG_HEADER]
    host = section['host']
    one_line_projects = section['list_of_projects'].split(',')
    list_of_projects = [x.strip() for x in one_line_projects]

    if host == '':
        print('Please define a HOST')
        return

    projects = '|'.join(list_of_projects)
    print('Watching for jira links')
    while True:
        clipboard = pyperclip.paste()
        if re.match('(' + projects + r')-\d+(\r\n)?$', clipboard):
            print(clipboard)
            pyperclip.copy('')
            webbrowser.open(
                f'https://{host}.atlassian.net/browse/{clipboard}')
        time.sleep(1)


if __name__ == '__main__':
    main()
