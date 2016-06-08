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
package com.gs.fw.common.freyaxml.generator.ant;

import com.gs.fw.common.freyaxml.generator.FreyaXmlException;
import com.gs.fw.common.freyaxml.generator.FreyaXmlGenerator;
import com.gs.fw.common.freyaxml.generator.Logger;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class FreyaAntTask extends Task implements Logger
{
    private int logLevel = Project.MSG_WARN;

    private FreyaXmlGenerator generator = new FreyaXmlGenerator();

    @Override
    public void execute() throws BuildException
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        this.generator.setLogger(this);
        try
        {
            this.generator.generate();
        }
        catch(FreyaXmlException e)
        {
            throw new BuildException("build failed with "+e.getClass().getName()+": "+e.getMessage(), e);
        }
        catch (IOException e)
        {
            throw new BuildException("build failed with "+e.getClass().getName()+": "+e.getMessage(), e);
        }
        Thread.currentThread().setContextClassLoader(loader);
    }

    public boolean isGenerateTopLevelSubstitutionElements()
    {
        return generator.isGenerateTopLevelSubstitutionElements();
    }

    public void setGenerateTopLevelSubstitutionElements(boolean generateTopLevelSubstitutionElements)
    {
        generator.setGenerateTopLevelSubstitutionElements(generateTopLevelSubstitutionElements);
    }

    public void setDestinationPackage(String destinationPackage)
    {
        generator.setDestinationPackage(destinationPackage);
    }

    public String getParserName()
    {
        return generator.getParserName();
    }

    public void setParserName(String parserName)
    {
        generator.setParserName(parserName);
    }

    public String getDestinationPackage()
    {
        return generator.getDestinationPackage();
    }

    public String getGeneratedDir()
    {
        return generator.getGeneratedDir();
    }

    public String getNonGeneratedDir()
    {
        return generator.getNonGeneratedDir();
    }

    public String getXsd()
    {
        return generator.getXsd();
    }

    public void setGeneratedDir(String generatedDir)
    {
        generator.setGeneratedDir(generatedDir);
    }

    public void setIgnoreNonGeneratedAbstractClasses(boolean ignoreNonGeneratedAbstractClasses)
    {
        generator.setIgnoreNonGeneratedAbstractClasses(ignoreNonGeneratedAbstractClasses);
    }

    public void setIgnorePackageNamingConvention(boolean ignorePackageNamingConvention)
    {
        generator.setIgnorePackageNamingConvention(ignorePackageNamingConvention);
    }

    public void setNonGeneratedDir(String nonGeneratedDir)
    {
        generator.setNonGeneratedDir(nonGeneratedDir);
    }

    public void setXsd(String xsd)
    {
        generator.setXsd(xsd);
    }

    public int getLogLevel()
    {
        return logLevel;
    }

    public void setLogLevel(int logLevel)
    {
        this.logLevel = logLevel;
    }

    private void logForLevel(String msg, int level)
    {
        if (this.logLevel >= level)
        {
            this.log(msg, level);
        }
    }

    private void logForLevel(String msg, Throwable t, int level)
    {
        if (this.logLevel >= level)
        {
            this.log(msg+": "+t.getClass().getName()+": "+t.getMessage(), level);
        }
    }

    public void info(String msg)
    {
        logForLevel(msg, Project.MSG_INFO);
    }

    public void info(String msg, Throwable t)
    {
        logForLevel(msg, t, Project.MSG_INFO);
    }

    public void warn(String msg)
    {
        logForLevel(msg, Project.MSG_WARN);
    }

    public void warn(String msg, Throwable t)
    {
        logForLevel(msg, t, Project.MSG_WARN);
    }

    public void error(String msg)
    {
        logForLevel(msg, Project.MSG_ERR);
    }

    public void error(String msg, Throwable t)
    {
        logForLevel(msg, t, Project.MSG_ERR);
    }

    public void debug(String msg)
    {
        logForLevel(msg, Project.MSG_DEBUG);
    }

    public void debug(String msg, Throwable t)
    {
        logForLevel(msg, t, Project.MSG_DEBUG);
    }

}
