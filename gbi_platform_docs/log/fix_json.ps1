 = Get-Content 'D:\Office\Project\Java\gbi_platform\gbi_platform_docs\attachments\sql\upgrade_v2.0_salary_system_v6.sql' -Raw  
 = .Replace('\{', '{').Replace('\}', '}')  
[IO.File]::WriteAllText('D:\Office\Project\Java\gbi_platform\gbi_platform_docs\attachments\sql\upgrade_v2.0_salary_system_v6.sql', , (New-Object System.Text.UTF8Encoding False)) 
