DynamicProvisionDriver
------------------------

~~Start server:~~

~~export IPL_ADDRESS="$(hostname -I)"~~

~~export SERVER_HOME= < Webservice >~~

Load configuration: 

Important! Please check the settings to make it suitable to your current environment.

`source scripts/setting.conf` 

Start service:

`./scripts/DynPrvServer-run`

Start application:

`./scripts/ipl-run`

Prun:

`prun -1 -np 2 -t 10:00:00 scripts/ipl-run`