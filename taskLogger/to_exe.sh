rm -rf _internal build dist
pyinstaller tasklogger.py --noconsole --noconfirm --clean --exclude-module xlrd --exclude-module xlwt --exclude-module openpyxl
cp -a dist/tasklogger/. .