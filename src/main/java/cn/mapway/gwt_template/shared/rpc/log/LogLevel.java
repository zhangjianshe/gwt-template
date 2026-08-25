package cn.mapway.gwt_template.shared.rpc.log;

import lombok.Getter;

@Getter
public enum LogLevel {
    INFO(0,"INFO"),
    WARN(1,"WARN"),
    ERROR(2,"ERROR"),
    DEBUG(3,"DEBUG");

    Integer level;
    String name;
    LogLevel(Integer level,String name)
    {
        this.level=level;
        this.name=name;
    }

    public static LogLevel fromLevel(Integer level)
    {
        if(level==null)
        {
            return INFO;
        }
        for (LogLevel logLevel:LogLevel.values())
        {
            if (logLevel.level.equals(level))
            {
                return logLevel;
            }
        }
        return INFO;
    }
}
