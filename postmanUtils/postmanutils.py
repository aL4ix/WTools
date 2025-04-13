import argparse
import csv
import json


def parse_postman_collection(collection_json, pattern):
    test_cases = []

    def process_item(item):
        # Process all items recursively
        if isinstance(item, dict) and 'item' in item:
            for sub_item in item['item']:
                process_item(sub_item)

        # Check if current item is a request that matches the pattern
        if isinstance(item, dict) and 'request' in item:
            if 'name' in item and pattern.lower() in item['name'].lower():
                request = item['request']
                method = request.get('method', '')
                url_raw = request.get('url', {}).get('raw', '')
                body = request.get('body', {}).get('raw', '')

                # Extract test script from events
                tests = []
                for event in item.get('event', []):
                    if event.get('listen') == 'test' and 'script' in event:
                        tests.extend(event['script']['exec'])

                step = f'Call {method} {url_raw}\n\nWith body like:\n{body}'

                # Add to test cases
                test_cases.append({
                    'step': step,
                    'expected': 'Status code 200 with a response like follows:\n\n' + '\n'.join(tests) if tests else ''
                })

    # Start processing from root items
    for root_item in collection_json.get('item', []):
        process_item(root_item)

    return test_cases


def save_to_csv(test_cases, output_file):
    with open(output_file, 'w', newline='', encoding='utf-8') as csvfile:
        fieldnames = ['Step', 'Expected']
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

        writer.writeheader()

        for case in test_cases:
            writer.writerow({
                'Step': case['step'],
                'Expected': case['expected']
            })


def main():
    parser = argparse.ArgumentParser(
        description='Extract test cases from Postman collection'
    )
    parser.add_argument(
        '-f',
        '--file',
        type=str,
        required=True,
        help='Path to the Postman collection JSON file'
    )
    parser.add_argument(
        '-p',
        '--pattern',
        type=str,
        required=True,
        help='Search pattern for test case names'
    )
    args = parser.parse_args()

    with open(args.file, 'r', encoding='utf-8') as f:
        postman_collection = json.load(f)

    test_cases = parse_postman_collection(postman_collection, args.pattern)

    output_file = 'test_cases.csv'
    save_to_csv(test_cases, output_file)

    print(f"Extracted {len(test_cases)} test cases and saved them to {output_file}")


if __name__ == '__main__':
    main()
