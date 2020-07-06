DynamicProvisionDriver
------------------------

Start server:

~~export IPL_ADDRESS="$(hostname -I)"~~

~~export SERVER_HOME= < Webservice >~~

`source scripts/setting.conf` 

Start service:

`./scripts/DynPrvServer-run`

Start job:

`./scripts/ipl-run nl.esciencecenter.DynPrvDriver.Driver -m`

