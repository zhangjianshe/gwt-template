

这个目录 存放一个基本的IT运行环境部署系统

step1: 
```shell
 rm -rf ./data/dev/data/*
 rm -rf ./data/ldap/*
 rm -rf ./pdns-db/*
 rm -rf ./step-ca-data/*
 rm -rf ./traefik-certs/*
```

step2:
```shell
./init.sh
     tjj.cn  根证书名称
     setp-ca,localhost,127.0.0.1,ca.tjj.cn
     QAZwsx@1234
```

step3:
```shell
docker-compose up -d
./init-acme.sh
```

step4:
  配置DNS localhost:7700 powerdns
http://powerdns:8081
let_china_great_again



setup cert default setting 


./data/step-ca-data/config/ca.json

`{
"type": "ACME",
"name": "satway-acme",
"claims": {
"enableSSHCA": true,
"disableRenewal": false,
"allowRenewalAfterExpiry": true,
"disableSmallstepExtensions": false,
"maxTLSCertDuration": "2160h",
"defaultTLSCertDuration": "2160h"
},
"options": {
"x509": {},
"ssh": {}
}
}`