package com.gs.fw.common.freyaxml.generator;
/*
Copyright 2016 Goldman Sachs.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */

import java.io.IOException;
import java.util.Date;

public class FreyaXmlCmd
{
    public static void main(String[] args)
    {
        FreyaXmlGenerator generator = new FreyaXmlGenerator();
        generator.setParserName(args[0]);
        generator.setXsd(args[1]);
        generator.setDestinationPackage(args[2]);
        generator.setGeneratedDir(args[3]);
        generator.setNonGeneratedDir(args[4]);
        int logLevel = CmdLogger.INFO_LEVEL;
        if (args.length == 6)
        {
            logLevel = Integer.parseInt(args[0]);
        }
        CmdLogger logger = new CmdLogger(logLevel);
        generator.setLogger(logger);
        try
        {
            generator.generate();
        }
        catch (Exception e)
        {
            logger.error("Could not generate", e);
            System.exit(-1);
        }
    }

    private static class CmdLogger implements Logger
    {
        public static final int DEBUG_LEVEL = 10;
        public static final int INFO_LEVEL = 20;
        public static final int WARN_LEVEL = 30;
        public static final int ERROR_LEVEL = 40;

        private int level;

        private CmdLogger(int level)
        {
            this.level = level;
        }

        @Override
        public void debug(String msg)
        {
            report(DEBUG_LEVEL, "DEBUG", msg);
        }

        private void report(int level, String levelMsg, String msg)
        {
            if (this.level <= level)
            {
                System.out.print(new Date().toString());
                System.out.print(": ");
                System.out.print(levelMsg);
                System.out.print(" ");
                System.out.println(msg);
            }
        }

        private void report(int level, String levelMsg, Throwable t)
        {
            if (this.level <= level)
            {
                System.out.print(new Date().toString());
                System.out.print(": ");
                System.out.print(levelMsg);
                System.out.print(" ");
                System.out.print(t.getClass().getName());
                System.out.print(": ");
                System.out.println(t.getMessage());
                t.printStackTrace();
                Throwable cause = t.getCause();
                while(cause != null)
                {
                    System.out.print("Caused by: ");
                    System.out.print(cause.getClass().getName());
                    System.out.print(": ");
                    System.out.println(cause.getMessage());
                    cause.printStackTrace();
                    cause = cause.getCause();
                }
            }
        }

        @Override
        public void info(String msg)
        {
            report(INFO_LEVEL, "INFO", msg);
        }

        @Override
        public void warn(String msg)
        {
            report(WARN_LEVEL, "WARN", msg);
        }

        @Override
        public void error(String msg)
        {
            report(ERROR_LEVEL, "ERROR", msg);
        }

        @Override
        public void info(String msg, Throwable t)
        {
            report(INFO_LEVEL, "INFO", msg);
            report(INFO_LEVEL, "INFO", t);
        }

        @Override
        public void warn(String msg, Throwable t)
        {
            report(WARN_LEVEL, "WARN", msg);
            report(WARN_LEVEL, "WARN", t);
        }

        @Override
        public void error(String msg, Throwable t)
        {
            report(ERROR_LEVEL, "ERROR", msg);
            report(ERROR_LEVEL, "ERROR", t);
        }

        @Override
        public void debug(String msg, Throwable t)
        {
            report(DEBUG_LEVEL, "DEBUG", msg);
            report(DEBUG_LEVEL, "DEBUG", t);
        }
    }
}
