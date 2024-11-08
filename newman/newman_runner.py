import codecs
import configparser
import json
import subprocess
from typing import Dict, List

CONFIG_HEADER = 'Configuration'


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


def runner():
    config = configparser.ConfigParser()
    config.read_file(open('configuration.ini'))
    url = config[CONFIG_HEADER]['url']

    subprocess.check_call(f'curl --output collection.json {url}', shell=True)
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


if __name__ == '__main__':
    runner()
