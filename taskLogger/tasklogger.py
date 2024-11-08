import datetime
import tkinter as tk
from tkinter import simpledialog
from tkinter import ttk

import pandas as pd

REPORT_NAME_FMT = 'log-{}.csv'
REPORT_COLUMNS = ['name', 'time']
ADD_NEW_TASK = 'Add new task'
SOMETHING = 'Something'
FINISH = 'Finish'
NATIVE_TASKS = (ADD_NEW_TASK, SOMETHING, FINISH, 'SH', 'GO', 'Meetings')


def main():
    root = tk.Tk()
    root.title("TL")

    today = datetime.date.today()
    report_name = REPORT_NAME_FMT.format(today)
    list_of_tasks = list(NATIVE_TASKS)
    last_task = ''
    try:
        df = pd.read_csv(report_name)
        print(f'Reading from existent file {report_name}')
        my_tasks = df.to_dict('records')
        tasks_from_file = df.name.unique().tolist()
        if SOMETHING in tasks_from_file: tasks_from_file.remove(SOMETHING)
        list_of_tasks.extend(tasks_from_file)
        if len(df) > 0:
            last_task = df.iloc[-1][REPORT_COLUMNS[0]]
    except FileNotFoundError:
        my_tasks = []
        print(f'Starting new file {report_name}')

    current_task_str_var = tk.StringVar()
    combo = ttk.Combobox(root, textvariable=current_task_str_var, state='readonly')
    combo.pack(side=tk.LEFT, expand=True, fill=tk.X)
    edit_btn = ttk.Button(root, text="Edit")
    edit_btn.pack(side=tk.LEFT)

    combo['values'] = list_of_tasks
    combo.set(last_task)

    def combo_changed_event(event):
        task_to_add = current_task_str_var.get()
        if task_to_add == ADD_NEW_TASK:
            new_task = simpledialog.askstring('New task name?', '')
            if new_task in (None, ''):  # Validation
                combo.set(SOMETHING)
                return
            current = list(combo['values'])
            if new_task not in current:  # Add
                current.append(new_task)
                combo['values'] = current
            task_to_add = new_task  # Set new
        combo.set(task_to_add)  # Set not new
        my_tasks.append({REPORT_COLUMNS[0]: task_to_add,
                         REPORT_COLUMNS[1]: datetime.datetime.now()})

    def edit_event(event):
        universal_startfile(report_name)

    def close_root_event():
        temp_df = pd.DataFrame(my_tasks, columns=REPORT_COLUMNS)
        print(temp_df)
        temp_df.to_csv(report_name)
        root.destroy()

    combo.bind('<<ComboboxSelected>>', combo_changed_event)
    bind_button(edit_btn, edit_event)
    root.protocol("WM_DELETE_WINDOW", close_root_event)

    root.mainloop()


def bind_button(remove_btn, remove_event):
    remove_btn.bind('<Return>', remove_event)
    remove_btn.bind('<Button-1>', remove_event)


def universal_startfile(filepath):
    import subprocess, os, platform
    if platform.system() == 'Darwin':
        subprocess.call(('open', filepath))
    elif platform.system() == 'Windows':
        os.startfile(filepath)
    else:
        subprocess.call(('xdg-open', filepath))


if __name__ == '__main__':
    main()
