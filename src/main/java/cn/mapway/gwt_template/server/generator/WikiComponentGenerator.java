package cn.mapway.gwt_template.server.generator;

import cn.mapway.gwt_template.shared.wiki.DummyWikiComponentManager;
import cn.mapway.gwt_template.shared.wiki.WikiComponentManager;
import cn.mapway.gwt_template.shared.wiki.component.IWikiComponent;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponent;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponentInformation;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WikiComponentGenerator extends Generator {
    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
        TypeOracle oracle = context.getTypeOracle();

        // 1. 搜集所有组件
        List<JClassType> componentTypes = new ArrayList<>();
        for (JClassType type : oracle.getTypes()) {
            if (type.getAnnotation(WikiComponent.class) != null) {
                componentTypes.add(type);
            }
        }

        // 2. 准备生成类信息
        String packageName = WikiComponentManager.class.getPackage().getName();
        String className = "GeneratedWikiComponentManager";

        PrintWriter pw = context.tryCreate(logger, packageName, className);
        if (pw == null) return packageName + "." + className;

        ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(packageName, className);
        composer.setSuperclass(DummyWikiComponentManager.class.getName());
        composer.addImport(WikiComponentInformation.class.getName());
        composer.addImport(IWikiComponent.class.getName());
        composer.addImport(WikiComponent.class.getName());
        SourceWriter sw = composer.createSourceWriter(context, pw);

        // 3. 重写构造函数或初始化方法
        // 我们利用覆写来实现，而不是真正的文本替换（GWT Generator 更倾向于继承或重写）
        sw.println("@Override");
        sw.println("protected void initComponents() {");
        sw.indent();
        sw.println("super.initComponents();"); // 保留父类可能有的基础组件

        // 在循环前增加一个 Set 来检测重复的 kind
        Set<String> seenKinds = new HashSet<>();
        for (JClassType type : componentTypes) {
            WikiComponent ann = type.getAnnotation(WikiComponent.class);
            if (!seenKinds.add(ann.kind())) {
                logger.log(TreeLogger.Type.WARN, "发现重复的 WikiComponent kind: " + ann.kind() + " 在类 " + type.getName());
                continue;
            }
            sw.println("{");
            sw.println("  WikiComponentInformation info = new WikiComponentInformation();");
            sw.println("  info.setKind(\"" + ann.kind() + "\");");
            sw.println("  info.setName(\"" + ann.name() + "\");");
            sw.println("  info.setUnicode(\"" + ann.unicode() + "\");");
            sw.println("  info.setCatalog(\"" + ann.catalog() + "\");");
            sw.println("  info.setSummary(\"" + ann.summary() + "\");");
            sw.println("  getComponentsMetadata().add(info);");
            sw.println("}");
        }
        sw.outdent();
        sw.println("}");

        // 4. 重写 createComponent
        sw.println("@Override");
        sw.println("public IWikiComponent createComponent(String kind) {");
        sw.indent();
        sw.println("if (kind == null) return super.createComponent(null);");

        for (JClassType type : componentTypes) {
            WikiComponent ann = type.getAnnotation(WikiComponent.class);
            sw.println("if (\"" + ann.kind() + "\".equals(kind)) return new " + type.getQualifiedSourceName() + "();");
        }

        sw.println("return super.createComponent(kind);");
        sw.outdent();
        sw.println("}");

        sw.commit(logger);
        return packageName + "." + className;
    }
}
