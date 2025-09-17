pyinstaller tasklogger.py --noconsole --noconfirm --clean --add-data "ssie.py;." --exclude-module xlrd --exclude-module xlwt --exclude-module openpyxl
xcopy dist\tasklogger . /s /y /e /h
