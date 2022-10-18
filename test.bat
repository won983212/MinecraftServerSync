@echo off
:_loop
set /a a+=1
echo Server Running Loop %a%
timeout 1 > NUL
if %a%==10 goto _break
goto _loop
:_break
echo Server End.