@echo off
cd /d "%~dp0"
echo Instalando dependencias...
python -m pip install flask --quiet
echo.
echo Iniciando CADView...
echo Acesse http://localhost:5000 no navegador
echo.
python server.py
echo.
pause