{
	"root": "{{ .NodeKey | defaults "certs/root_ca.crt" }}",
	"federatedRoots": null,
	"crt": "{{ .NodeKey | defaults "certs/intermediate_ca.crt" }}",
	"key": "{{ .NodeKey | defaults "secrets/intermediate_ca_key" }}",
	"address": "{{ .Address }}",
	"insecureAddress": "{{ .InsecureAddress }}",
	"dnsNames": {{ .DNSNames | toJson }},
	"logger": {
		"format": "text"
	},
	"db": {
		"type": "badgerv2",
		"dataSource": "{{ .NodeKey | defaults "db" }}",
		"badgerFileLoadingMode": ""
	},
	"authority": {
		"enableAdmin": true,
		"provisioners": [
			{
				"type": "ACME",
				"name": "satway-acme",
				"claims": {
					"minTLSCertDuration": "5m",
					"maxTLSCertDuration": "2160h",
					"defaultTLSCertDuration": "2160h"
				}
			}
		]
	},
	"tls": {{ .TLS | toJson }}
}