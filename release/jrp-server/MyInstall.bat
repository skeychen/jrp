@echo on
set my=MyJrpService
net stop %my%
sc delete %my%
@echo �س�����а�װ������ֱ�ӹرմ���
pause

set p=%~dp0
set p=%p:\=/%
sc create %my% binPath= %p%%my%.exe
sc config %my% start= auto type= share
@echo �س�����з�������������ֱ�ӹرմ���
pause

net start %my%
@echo �س���رմ���
pause
