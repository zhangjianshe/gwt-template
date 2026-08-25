NOW=$(TZ='Asia/Shanghai' date +'%Y-%m-%d-%H:%M:%S')
curl -k -X POST "http://localhost:8080/api/v1/software/upload" \
        -F "token=dd00b5b5edfb4658845e6e4d9886411d" \
        -F "version=$NOW" \
        -F "name=gwt-template-1.0.0.jar" \
        -F "summary=build @$NOW" \
        -F "os=all platform" \
        -F "arch=arm64+x86" \
        -F "file=@../target/gwt-template-1.0.0-SNAPSHOT.jar"