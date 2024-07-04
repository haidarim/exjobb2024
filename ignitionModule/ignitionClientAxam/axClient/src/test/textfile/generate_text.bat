@echo off
setlocal enabledelayedexpansion

if "%~3"=="" (
    echo Usage: %~0 ^<source_file^> ^<size_in_MB^> ^<output_file^>
    exit /b 1
)

set source_file=%~1
set size_MB=%~2
set output_file=%~3

:: Calculate the size of the source file in bytes
for %%a in (%source_file%) do set source_size=%%~za

:: Calculate the total size in bytes
set /a total_size=%size_MB% * 1024 * 1024

:: Clear the output file if it exists
if exist "%output_file%" del "%output_file%"

:: Generate the output file by repeating the source file
set /a bytes_written=0
:write_loop
set /a remaining_bytes=%total_size% - %bytes_written%
if %remaining_bytes% leq 0 goto :done

if %remaining_bytes% lss %source_size% (
    :: Write only the remaining bytes needed to reach the desired size
    break < "%source_file%" > NUL 2>&1
    for /L %%i in (1,1,%remaining_bytes%) do (
        set /p "char=" < "%source_file%"
        <nul set /p =%char% >> "%output_file%"
        set /a bytes_written+=1
        if !bytes_written! geq %total_size% goto :done
    )
) else (
    :: Write the whole source file
    type "%source_file%" >> "%output_file%"
    set /a bytes_written+=%source_size%
    goto :write_loop
)

:done

echo Generated %output_file% with size %size_MB% MB
