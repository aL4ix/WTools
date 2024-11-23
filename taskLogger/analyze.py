import glob
import math
import os

import pandas as pd


def create_reports():
    # Specify the pattern for the files
    file_pattern = "log-*.csv"

    # Use glob to find all files matching the pattern
    files = glob.glob(file_pattern)

    # Iterate through the files and extract names
    for file in files:
        # Extract the base name (e.g., 'report-2024-10-28.csv')
        base_name = os.path.basename(file)

        df = pd.read_csv(base_name, parse_dates=['time'])

        # Calculate the time spent on each task
        df['next_time'] = df['time'].shift(-1)
        df.loc[df.index[-1], 'next_time'] = df['time'].iloc[0] + pd.Timedelta(hours=8)
        df['time_spent'] = df['next_time'] - df['time']

        # Convert time_spent to hours
        df['time_spent_hours'] = df['time_spent'].dt.total_seconds() / 3600

        # Optionally, format to ensure two decimal places are always shown
        df['time_spent_hours'] = df['time_spent_hours'].apply(lambda x: f"{x:.2g}")

        # Round to the nearest 0.5 hours
        df['time_spent_rounded'] = (df['time_spent_hours'].astype(float) * 2).round() / 2

        # Select relevant columns
        report_df = df[['name', 'time_spent_hours']]  # , 'time_spent_rounded']]

        # Export to CSV
        output_file = f'report-{base_name}'
        report_df.to_csv(output_file, index=False)

        print(f"Data exported to {output_file}")


def merge_reports():
    # Specify the pattern for the files
    file_pattern = "report-log-*.csv"

    # Use glob to find all files matching the pattern
    files = glob.glob(file_pattern)

    # Initialize a list to hold DataFrames
    dataframes = []

    # Iterate through the files and read them into DataFrames
    for file in files:
        df = pd.read_csv(file)  # Read the CSV file

        # Sum time_spent_hours for each name
        summary = df.groupby('name', as_index=False)['time_spent_hours'].sum()

        # Extract the day from the filename
        day = file.split('-')[-1].split('.')[0]  # Extract the date part (e.g., '2024-10-18' -> '18')
        summary['day'] = day  # Add the day to the summary DataFrame
        dataframes.append(summary)  # Append the summary DataFrame to the list

    # Concatenate all the summaries into a single DataFrame
    merged_df = pd.concat(dataframes, ignore_index=True)

    # Pivot the DataFrame to have names as rows and days as columns
    pivoted_df = merged_df.pivot_table(
        index='name',
        columns='day',
        values='time_spent_hours',
        fill_value=0
    ).reset_index()

    # Optionally, remove the name of the columns index
    pivoted_df.columns.name = None

    # Format the values in the pivoted DataFrame to show only 2 decimal places
    for col in pivoted_df.columns[1:]:  # Skip the 'name' column

        def helper(x):
            x = math.ceil(x * 2) / 2
            return f"{x:.2g}"

        pivoted_df[col] = pivoted_df[col].apply(lambda y: helper(y))

    # Display the result
    pivoted_df.rename(columns={'name': 'Title'}, inplace=True)
    pivoted_df.to_csv('merged_report.csv', index=False)
    print('Created merged report')


if __name__ == '__main__':
    create_reports()
    # merge_reports()
