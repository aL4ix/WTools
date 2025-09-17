import json
import os
import pathlib
import re
import shutil
import configparser
from dataclasses import dataclass

import ollama
from ollama import Client

INDENTATION = '        '
SECOND_INDENTATION = '    ' + INDENTATION
ITEM = 'item'


@dataclass
class Config:
    model: str
    limit_tcs_num: bool
    total_tc: int
    send_to_ai: bool


def create_safe_file_name(file_name) -> str:
    safe_name = re.sub(r'[ .\-<>:"/\\|?*\x00-\x1F=&,]', '_', file_name)
    safe_name = re.sub(r'[{}\[\]()\']', '', safe_name)
    safe_name = safe_name.strip()
    return safe_name


def to_camel_case(snake_str: str, title_case=False):
    words = snake_str.split('_')
    first_word = words[0].lower()
    if title_case:
        if len(first_word) > 0:
            first_word = first_word[0].upper() + first_word[1:]
    return first_word + ''.join(word.capitalize() for word in words[1:])


def reverse_interpolate(input_str):
    pattern = re.compile(r'\{\{(.*?)}}')

    parts = []
    last_end = 0

    for match in pattern.finditer(input_str):
        start, end = match.span()
        var_name = match.group(1)

        if start > last_end:
            parts.append(f'"{input_str[last_end:start]}"')

        parts.append(f'environment.get("{var_name}")')

        last_end = end

    if last_end < len(input_str):
        parts.append(f'"{input_str[last_end:]}"')

    return ' + '.join(parts)


def start_navigating_postman(collection, collection_folder, config_data: Config, client: ollama.Client):
    with open(collection, encoding='utf-8') as f:
        json_obj = json.load(f)
    root = pathlib.Path('.') / 'tests' / collection_folder
    root.mkdir(parents=True, exist_ok=True)
    navigate_postman(json_obj, root, root, config_data, client)
    # Close
    for dirpath, _, filenames in os.walk(root):
        for filename in filenames:
            file_path = os.path.join(dirpath, filename)
            with open(file_path, 'a', encoding='utf-8') as file:
                file.write('}')


def navigate_postman(json_obj, root: pathlib.Path, path: pathlib.Path, config_data: Config, client: ollama.Client):
    items = json_obj.get(ITEM)
    for item in items:
        name = item['name']
        safe_name = create_safe_file_name(name)
        if item.get(ITEM):
            new_folder_or_file = path / safe_name
            for i in item[ITEM]:
                if i.get(ITEM):
                    print(f'FOLDER {name}')
                    if name == 'Before Tests':
                        print('Skipping Before Tests')
                        break

                    new_folder_or_file.mkdir(exist_ok=True)
                    navigate_postman(item, root, new_folder_or_file, config_data, client)
                    break
            else:
                print(f'S      {name}')
                for inner_item in item['item']:
                    create_test_per_request(inner_item, new_folder_or_file, config_data, client)
        else:
            print(f'A      {name}')
            create_test_per_request(item, path, config_data, client)


def process_script(header, pm_script, config_data, client):
    translated = indent_text(send_to_ai(pm_script, config_data, client), INDENTATION)
    pm_script = indent_text(pm_script, SECOND_INDENTATION)
    pm_script = f'// {header}\n{INDENTATION}String javaScript{header} = """\n{pm_script}""";{translated}'
    return pm_script


def send_to_ai(code: str, config_data: Config, client: ollama.Client) -> str:
    if not config_data.send_to_ai:
        return ''
    if len(code.strip()) == 0:
        return code
    response = client.generate(
        model=config_data.model,
        prompt=f"""Your job is to translate postman scripts to Java/Restassured, I have already an `environment` object that has a `environment.get()` and a `environment.put()`.
I also have a Utils class that lists static methods for `regexMatch`, `encodeURIComponent`, `decodeURIComponent`.
I have a Log class that exposes a `.info`.
Use testng assertions.
For each postman test, when you see a `pm.test`, I have a `void testRailTest(String title, Runnable runnable)` use it in the case there is a postman test.
Assume `response` is the RestAssured Response object, if you create a new request don't name the response `response`. Any RestAssured `given()` do it with `getGlobalRestAssuredGiven()`.
Use raw triple string literals assigned to a variable for body declaration, don't use programmatic creation.
Translate this, no need to add extra code, extra requests and only put the contents that go inside the method, no introduction, no explanation: ```
{code}```""",
        stream=False  # set to True for streaming output
    )
    print("Answer:", response["response"])
    res = response["response"].strip()
    if res.startswith("```java") and res.endswith("```"):
        res = res[7:-3]
    return f'\n//Generated by AI\n{INDENTATION}{res}'


def indent_text(text: str, indent: str):
    lines = text.splitlines()
    return '\n'.join(indent + line for line in lines)


def extract_body(request_item):
    body = ''
    body_item = request_item.get('body')
    if body_item:
        if 'raw' in body_item:
            body_raw = body_item['raw']
            body = f'// Body\n{INDENTATION}// language=JSON\n{INDENTATION}spec.body("""\n{indent_text(body_raw, SECOND_INDENTATION)}""");'
        else:
            body = f'// Body\n{INDENTATION}// This request has no normal body, the following comment includes the postman "body" object\n{INDENTATION}// {repr(body_item)}'
    return body


def extract_request(request_item):
    request = '// Request'
    method = request_item['method'].lower()
    url = request_item['url']['raw']
    needs_variables = ''
    pattern = r"(?<=\/):[a-zA-Z0-9_]+"
    if bool(re.search(pattern, url)):
        needs_variables = ', variables'
    request += f'\n{INDENTATION}Response response = spec.when().{method}(interpolate("{url}"{needs_variables}));\n{INDENTATION}response.then().log().all();'
    return request


def extract_headers(request_item):
    headers = ''
    header_items = request_item['header']
    header_dict = {}
    for header_item in header_items:
        header_dict[header_item['key']] = header_item['value']
    if header_dict:
        headers = """// Headers
        RequestSpecification spec = getGlobalRestAssuredGiven()
            .headers(new HashMap<String, String>() {{\n"""
        for header_key in header_dict:
            headers += SECOND_INDENTATION + f'put("{header_key}", {reverse_interpolate(header_dict[header_key])});\n'
        headers += INDENTATION + '}});'
    return headers


def extract_variables(request_item):
    variables = ''
    variable_items = request_item['url'].get('variable')
    if not variable_items:
        return variables
    variable_dict = {}
    for variable_item in variable_items:
        variable_dict[variable_item['key']] = variable_item['value']
    if variable_dict:
        variables = """// Variables
        Map<String, String> variables = new HashMap<>() {{\n"""
        for header_key in variable_dict:
            variables += SECOND_INDENTATION + f'put("{header_key}", {reverse_interpolate(variable_dict[header_key])});\n'
        variables += INDENTATION + '}};'
    return variables


def extract_scripts(item):
    pre_script = ''
    post_script = ''
    events = item.get('event')
    if events:
        for event in events:
            listen = event.get('listen')
            if listen:
                exec_ = event['script'].get('exec')
                if exec_:
                    pm_script = '\n'.join(exec_).strip()
                    if listen == 'prerequest':
                        if pm_script:
                            pre_script = f'{pm_script}'
                    if listen == 'test':
                        if pm_script:
                            post_script = f'{pm_script}'

    return post_script, pre_script


def create_test_per_request(item, path: pathlib.Path, config_data: Config, client: ollama.Client):
    class_name = to_camel_case(path.name, True)
    path_with_suffix = path.parent / f'{class_name}.java'
    if not path_with_suffix.exists():

        package = str(pathlib.Path(*path.parts[1:]).parent).replace(os.sep, '.')
        with open(path_with_suffix, 'w', encoding='utf-8') as f:
            f.write(f"""package org.hbr.tests.{package};

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hbr.tests.restassuredframework.BaseTest;
import org.hbr.tests.restassuredframework.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.hbr.tests.restassuredframework.Utils.*;
import static org.testng.Assert.*;

public class {class_name} extends BaseTest {{""")

    with open(path_with_suffix, 'a', encoding='utf-8') as f:
        test_case_name = to_camel_case(create_safe_file_name(item['name']))

        post_script, pre_script = extract_scripts(item)
        pre_script = process_script('PreScript', pre_script, config_data, client)
        post_script = process_script('PostScript', post_script, config_data, client)
        request_item = item.get('request')
        if request_item:
            variables = extract_variables(request_item)
            headers = extract_headers(request_item)
            body = extract_body(request_item)
            request = extract_request(request_item)
        else:
            variables = ''
            headers = ''
            body = ''
            request = ''

        f.write(f"""
    @Test
    void {test_case_name}() {{
        {pre_script}
        {headers}
        {variables}
        {body}
        {request}
        {post_script}
    }}
""")
    if config_data.limit_tcs_num:
        config_data.total_tc += 1
        if config_data.total_tc >= 5:
            exit(0)


def main():
    DEFAULT_SECTION = 'DEFAULT'
    config = configparser.ConfigParser()
    config_file = 'configuration.ini'
    config.read(config_file)

    host = config.get(DEFAULT_SECTION, 'HOST')
    port = config.getint(DEFAULT_SECTION, 'PORT')
    config_data = Config(
    model=config.get(DEFAULT_SECTION, 'MODEL'),
    limit_tcs_num=config.getboolean(DEFAULT_SECTION, 'LIMIT_TCS_NUM'),
    total_tc=config.getint(DEFAULT_SECTION, 'TOTAL_TC'),
    send_to_ai=config.getboolean(DEFAULT_SECTION, 'SEND_TO_AI')
)

    shutil.rmtree('tests', ignore_errors=True)
    client = Client(host=f"{host}:{port}")

    start_navigating_postman("Learning_Destinations_ms_user.postman_collection.json",
                             'user', config_data, client)


if __name__ == '__main__':
    main()
