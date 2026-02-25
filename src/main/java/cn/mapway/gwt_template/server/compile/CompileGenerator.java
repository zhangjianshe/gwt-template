package cn.mapway.gwt_template.server.compile;

import cn.mapway.gwt_template.shared.compile.CompileFactory;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * CompileGenerator
 *
 * @author zhangjianshe@gmail.com
 */
@Slf4j
public class CompileGenerator extends Generator {
    static boolean hasGenerator = false;

    @Override
    public String generate(TreeLogger treeLogger, GeneratorContext generatorContext, String s) throws UnableToCompleteException {
        // 生成代理类的package
        final String genPackageName = CompileFactory.class.getPackageName();

        // 代理类名称
        final String genClassName = "CompileFactoryImpl";


        // 代码生成器工厂类
        ClassSourceFileComposerFactory composer =
                new ClassSourceFileComposerFactory(genPackageName, genClassName);


        // 代理类继承需要代理的接口
        composer.addImplementedInterface(CompileFactory.class.getCanonicalName());

        // 代理类要引用的类包
        //composer.addImport("cn.satway.cis.client.util.compile.*");
        composer.addImport(Date.class.getCanonicalName());

        // 创建一个源代码生成器对象
        PrintWriter printWriter = generatorContext.tryCreate(treeLogger, genPackageName, genClassName);

        if (printWriter != null) {
            // 源代码生成器
            SourceWriter sourceWriter = composer.createSourceWriter(generatorContext, printWriter);
            // 生成一个无参数构造函数
            sourceWriter.println("CompileFactoryImpl() {");
            sourceWriter.println("}");

            // 输出代码方法
            printFactoryMethod(sourceWriter);

            // 写入磁盘
            sourceWriter.commit(treeLogger);
        }
        //hasGenerator=true;
        log.info("已经生成了{}", s);
        // 返回生成的代理对象类名称
        return composer.getCreatedClassName();
    }

    private void printFactoryMethod(SourceWriter sourceWriter) {
        log.info("================ compile info generator===========");
        sourceWriter.println("public CompileInformation compileInfo(){");
        sourceWriter.println(" CompileInformation data=new CompileInformation();");
        String exec = "git --no-pager  log -n 1 --pretty=format:\"%h%n%cE%n%cI\"";
        String commitHash = "";
        String commitAuthor = "";
        long commitTime = System.currentTimeMillis();
        try {
            StringBuilder stringBuilder = Lang.execOutput(exec);
            String ll = stringBuilder.toString().trim();
            ll = Strings.removeFirst(ll, '"');
            ll = Strings.removeLast(ll, '"');
            BufferedReader reader = new BufferedReader(Streams.utf8r(Lang.ins(ll)));
            List<String> lines = new ArrayList<String>();
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
            log.info("invoke git commit with {}", Json.toJson(lines));
            if (lines.size() >= 3) {
                commitHash = lines.get(0).trim();
                commitAuthor = lines.get(1);
                String t = lines.get(2).replaceAll("T", " ");
                commitTime = Times.D(t).getTime();
            } else {
                commitAuthor = "Unknown";
                commitTime = System.currentTimeMillis();
                commitHash = "Unknown";
            }
        } catch (Exception e) {
            e.printStackTrace();
            commitAuthor = "Unknown";
            commitTime = System.currentTimeMillis();
            commitHash = "Unknown";
        }
        log.info("{} {} {}", commitHash, commitAuthor, commitTime);
        sourceWriter.println("\t data.gitTime= new Date(" + commitTime + "L);");
        sourceWriter.println("\t data.gitCommit= \"" + commitHash + "\";");
        sourceWriter.println("\t data.gitAuthor= \"" + commitAuthor + "\";");
        sourceWriter.println("\t data.compileTime= new Date(" + System.currentTimeMillis() + "L);");

        sourceWriter.println(" return data;");
        sourceWriter.println("}");
    }
}
