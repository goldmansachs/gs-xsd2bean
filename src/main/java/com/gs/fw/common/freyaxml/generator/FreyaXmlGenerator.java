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
package com.gs.fw.common.freyaxml.generator;


import com.gs.fw.common.freyaxml.generator.xsd.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;

public class FreyaXmlGenerator
{
    public static final String DATED = "dated";
    public static final String READ_ONLY = "read-only";
    public static final String DATED_READ_ONLY = DATED+READ_ONLY;
    public static final String TRANSACTIONAL = "transactional";
    public static final String DATED_TRANSACTIONAL = DATED+TRANSACTIONAL;
    public static final String EMBEDDED_VALUE = "embedded-value";
    public static final String ENUMERATION = "enumeration";

    private static final int MD5_LENGTH = 32;
    private String md5 = null;
    private CRC32 crc32 = new CRC32();
    protected GenerationLog oldGenerationLog;
    protected GenerationLog newGenerationLog;
    private FreyaContext freyaContext;

    public static Logger logger;


    private String xsd;
    private String destinationPackage;
    private String parserName;
	private String generatedDir;
    private String nonGeneratedDir;
    private boolean validateAttributes = true;
    private boolean ignoreNonGeneratedAbstractClasses = false;
    private boolean ignorePackageNamingConvention = false;
    private boolean generateTopLevelSubstitutionElements = false;
    private ThreadLocal<FullFileBuffer> fullFileBufferThreadLocal = new ThreadLocal<FullFileBuffer>();
    private AwaitingThreadExecutor executor;
    private Throwable executorError;
    private ChopAndStickResource chopAndStickResource = new ChopAndStickResource(new Semaphore(Runtime.getRuntime().availableProcessors()),
            new Semaphore(IO_THREADS), new SerialResource());
    private ThreadLocal<ByteArrayOutputStream> byteArrayOutputStreamThreadLocal = new ThreadLocal<ByteArrayOutputStream>();
    private ThreadLocal<SourceFormatter> sourceFormatterThreadLocal = new ThreadLocal<SourceFormatter>();

    private static final List<String> TEMPLATES = Arrays.asList(
                                                "Abstract_jsp"
												);

    private static final List<String> SHARED_TEMPLATES = Arrays.asList(
                                                "UnmarshallerAbstract_jsp","MarshallerAbstract_jsp", "ParserException_jsp"
												);

    private StringBuilder errorLogs = new StringBuilder();
    private static final int IO_THREADS = 1;

    public boolean isGenerateTopLevelSubstitutionElements()
    {
        return generateTopLevelSubstitutionElements;
    }

    public void setGenerateTopLevelSubstitutionElements(boolean generateTopLevelSubstitutionElements)
    {
        this.generateTopLevelSubstitutionElements = generateTopLevelSubstitutionElements;
    }

    public boolean isValidateAttributes()
    {
        return validateAttributes;
    }

    public void setValidateAttributes(boolean validateAttributes)
    {
        this.validateAttributes = validateAttributes;
    }

    public String getParserName()
    {
        return parserName;
    }

    public void setParserName(String parserName)
    {
        this.parserName = parserName;
    }

    public String getDestinationPackage()
    {
        return destinationPackage;
    }

    public void setDestinationPackage(String destinationPackage)
    {
        this.destinationPackage = destinationPackage;
    }

    private ByteArrayOutputStream getByteArrayOutputStream()
    {
        ByteArrayOutputStream result = byteArrayOutputStreamThreadLocal.get();
        if (result == null)
        {
            result = new ByteArrayOutputStream(10000);
            byteArrayOutputStreamThreadLocal.set(result);
        }
        result.reset();
        return result;
    }

    private SourceFormatter getSourceFormatter()
    {
        SourceFormatter result = sourceFormatterThreadLocal.get();
        if (result == null)
        {
            result = new SourceFormatter();
            sourceFormatterThreadLocal.set(result);
        }
        return result;
    }

    private byte[] fastFormatCode(byte[] originalBytes) throws IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(originalBytes);
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(bais));

        SourceFormatter sourceFormatter = getSourceFormatter();
        sourceFormatter.init();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(originalBytes.length);
        PrintWriter writer = new PrintWriter(byteArrayOutputStream);
        String line = null;
        while((line = reader.readLine()) != null)
        {
            if (line.trim().length() > 0)
            {
                sourceFormatter.formatLine(line, writer);
            }
        }
        writer.flush();
        byteArrayOutputStream.flush();
        writer.close();
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    private void applyUniqueTemplates(AtomicInteger count)
    {
        applyTemplates(new SharedXsdType(freyaContext.getParserName()), count, SHARED_TEMPLATES);
    }

    private void applyTemplates(GeneratorType xsdType, AtomicInteger count, List templates)
	{
        String templatePackage = "com.gs.fw.common.freyaxml.templates."+xsdType.getTemplatePackage();
        for (Iterator iterator = templates.iterator(); iterator.hasNext();)
		{
			String name = (String) iterator.next();
            String outputFileSuffix;
            outputFileSuffix = name.substring(name.lastIndexOf('.') + 1, name.lastIndexOf('_'));
            generateAbstractClass(xsdType, count, templatePackage, outputFileSuffix, "");
            //for every abstract class generate a dummy subclass if one is not out
			//there already. The template for the base class is in Main.jsp
            if (outputFileSuffix.endsWith("Abstract"))
            {
                generateConcreteSubclass(xsdType, count, templatePackage, outputFileSuffix, "");
            }
        }
	}

    private void generateConcreteSubclass(GeneratorType xsdType, AtomicInteger count, String templatePackage, String templatePrefix, String nonInterfaceSuffix)
    {
        String outputFileSuffix;
        templatePrefix = templatePrefix.substring(0, templatePrefix.indexOf("Abstract"));
        outputFileSuffix = nonInterfaceSuffix+templatePrefix;

        generateFile(xsdType, count, templatePackage, templatePrefix, outputFileSuffix, false);
    }

    private void generateAbstractClass(GeneratorType xsdType, AtomicInteger count,
            String templatePackage, String templatePrefix, String nonInterfaceSuffix)
    {
        String outputFileSuffix;
        outputFileSuffix = nonInterfaceSuffix+templatePrefix;
        generateFile(xsdType, count, templatePackage, templatePrefix, outputFileSuffix, true);
    }

    private void generateFile(GeneratorType xsdType, AtomicInteger count, String templatePackage,
                             String templatePrefix, String foo, boolean replaceIfExists)
    {
        FreyaXmlTemplate servlet = newTemplate( templatePackage + "." + (templatePrefix.equals("")?"Main":templatePrefix) + "_jsp");

        generateJavaFileFromTemplate(xsdType, foo, servlet, replaceIfExists, count);
    }

    private FreyaXmlTemplate newTemplate(String name)
    {
		try
		{
			return (FreyaXmlTemplate) FreyaXmlGenerator.class.getClassLoader().loadClass(name).newInstance();
		}
		catch (Exception e)
		{
			throw new FreyaXmlException("unable to load template " + name + ", make sure you have compiled the templates", e);
		}
	}

    private void generateJavaFileFromTemplate(GeneratorType xsdType, String outputFileSuffix, FreyaXmlTemplate servlet, boolean replaceIfExists, AtomicInteger count)
    {
        String targetDir = replaceIfExists ? this.getGeneratedDir() : this.getNonGeneratedDir();
        File outDir = new File(targetDir, (this.getDestinationPackage()+xsdType.getSubPackage()).replace('.', '/'));
        File outFile = new File(outDir, xsdType.getJavaTypeName() + outputFileSuffix + ".java");
        if (outFile.exists() && !replaceIfExists)
        {
            return;
        }
        if (outFile.exists())
        {
            if (this.newGenerationLog.isSame(this.oldGenerationLog))
            {
                this.logger.debug("skipping " + outFile.getName() + " because it's old and the generator has not changed");
                return;
            }
        }

        generateJavaFile(xsdType, outputFileSuffix, servlet, outDir, outFile, count);
    }

    private void generateJavaFile(final GeneratorType xsdType, final String outputFileSuffix, final FreyaXmlTemplate servlet,
            final File outDir, final File outFile, final AtomicInteger count)
    {
        this.getExecutor().submit(new GeneratorTask(0) {
            public void run()
            {
                JspWriter writer = null;
                try
                {
                    getChopAndStickResource().acquireCpuResource();
                    byte[] result;
                    try
                    {
                        ByteArrayOutputStream byteArrayOutputStream = getByteArrayOutputStream();
                        writer = new JspWriter(byteArrayOutputStream);
                        HttpServletRequest request = new HttpServletRequest();
                        request.setAttribute("xsdType", xsdType);
                        request.setAttribute("freyaContext", freyaContext);
                        HttpServletResponse response = new HttpServletResponse(writer);
                        servlet._jspService(request, response);
                        writer.close();
                        writer = null;
                        byte[] originalBytes = byteArrayOutputStream.toByteArray();
                        result = fastFormatCode(originalBytes);
                    }
                    finally
                    {
                        getChopAndStickResource().releaseCpuResource();
                    }
                    getChopAndStickResource().acquireIoResource();
                    try
                    {
                        outDir.mkdirs();
                        copyIfChanged(result, outFile, count);
                    }
                    finally
                    {
                        getChopAndStickResource().releaseIoResource();
                    }
                }
                catch (IOException e)
                {
                    throw new FreyaXmlException("Error writing class "+getDestinationPackage()+xsdType.getSubPackage()+"."+xsdType.getJavaTypeName()+outputFileSuffix+
                            " "+e.getClass().getName()+": "+e.getMessage(), e);
                }
                finally
                {
                    if (writer != null)
                    {
                        writer.close();
                    }
                }
            }
        });
    }

    private FullFileBuffer getFullFileBuffer()
    {
        FullFileBuffer result = fullFileBufferThreadLocal.get();
        if (result == null)
        {
            result = new FullFileBuffer();
            fullFileBufferThreadLocal.set(result);
        }
        return result;
    }

    public void setIgnorePackageNamingConvention(boolean ignorePackageNamingConvention)
    {
        this.ignorePackageNamingConvention = ignorePackageNamingConvention;
    }

    protected String getMd5()
    {
        if (this.md5 == null)
        {
            this.md5 = "";
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/gs/fw/common/freyaxml/generator/freyaxmlgen.md5");
            if (is != null)
            {
                byte[] md5Bytes = new byte[MD5_LENGTH];
                try
                {
                    this.fullyRead(is, md5Bytes);
                    this.md5 = new String(md5Bytes);
                    is.close();
                    return this.md5;
                }
                catch (IOException e)
                {
                    this.logger.error("got an IOException reading md5 file " + e.getClass().getName() + ": " + e.getMessage(), e);
                }
            }
            else
            {
                this.logger.error("Could not find md5. Will regenerate everything");
            }
        }
        return this.md5;
    }

    private void fullyRead(InputStream is, byte[] bytes) throws IOException
    {
        int read = 0;
        while (read < bytes.length)
        {
            read += is.read(bytes, read, bytes.length - read);
        }
    }

    protected String getCrc()
    {
        String result = Long.toHexString(crc32.getValue());
        while(result.length() < 8)
        {
            result = "0" + result;
        }
        return result;
    }

    public String getXsd()
	{
		return this.xsd;
	}

	public void setXsd(String xsd)
	{
		this.xsd = xsd;
	}

    public String getGeneratedDir()
	{
		return this.generatedDir;
	}

	public void setGeneratedDir(String generatedDir)
	{
		this.generatedDir = generatedDir;
	}

    public String getNonGeneratedDir()
    {
        return this.nonGeneratedDir;
    }

    public void setNonGeneratedDir(String nonGeneratedDir)
    {
        this.nonGeneratedDir = nonGeneratedDir;
    }

    public void setIgnoreNonGeneratedAbstractClasses(boolean ignoreNonGeneratedAbstractClasses)
    {
        this.ignoreNonGeneratedAbstractClasses = ignoreNonGeneratedAbstractClasses;
    }

    public void generate() throws IOException
    {
        validateParams();
        freyaContext = new FreyaContext();
        freyaContext.setPackageName(this.destinationPackage);
        freyaContext.setParserName(this.parserName);
        XsdSchema xsdSchema = parseXsd();
        freyaContext.setXsdSchema(xsdSchema);
        freyaContext.setGenerateTopLevelSubstitutionElements(this.generateTopLevelSubstitutionElements);
        generate(xsdSchema, new File(this.xsd).getPath());
    }

    private void generate(XsdSchema xsdSchema, String xsdPath) throws IOException
    {
        long start = System.currentTimeMillis();
        this.oldGenerationLog = GenerationLog.readOldLog(this.getGeneratedDir(), xsdPath);
        this.newGenerationLog = new GenerationLog(this.getMd5(), this.getCrc());
        if (!newGenerationLog.isSame(this.oldGenerationLog))
        {
            this.newGenerationLog.writeLog(this.getGeneratedDir(), xsdPath);
        }
        boolean done = false;
        try
        {
            AtomicInteger count = new AtomicInteger();
            applyUniqueTemplates(count);
            List<XsdSimpleType> simpleTypes = xsdSchema.getXsdSimpleTypes();
            for(XsdSimpleType simpleType : simpleTypes)
            {
                if (simpleType.isEnumeration())
                {
                    applyTemplates(simpleType, count, TEMPLATES);
                }
            }
            List<XsdSimpleType> anonSimpleTypes = xsdSchema.getAnonymousSimpleTypes();
            for(XsdSimpleType anonSimpleType : anonSimpleTypes)
            {
                if (anonSimpleType.isEnumeration())
                {
                    applyTemplates(anonSimpleType, count, TEMPLATES);
                }
            }
            List<XsdComplexType> complexTypes = xsdSchema.getXsdComplexTypes();
            for(int i=0;i<complexTypes.size();i++)
            {
                applyTemplates(complexTypes.get(i), count, TEMPLATES);
            }
            List<XsdComplexType> anonComplexTypes = xsdSchema.getAnonymousComplexTypes();
            for(int i=0;i<anonComplexTypes.size();i++)
            {
                applyTemplates(anonComplexTypes.get(i), count, TEMPLATES);
            }
            List<XsdElement> elements = xsdSchema.getGlobalElements();
            for(int i=0;i<elements.size();i++)
            {
                if (elements.get(i).mustGenerate(generateTopLevelSubstitutionElements))
                {
                    applyTemplates(elements.get(i), count, TEMPLATES);
                }
            }
            waitForExecutorWithCheck();
            logger.info("Wrote "+count.get()+" files in "+(System.currentTimeMillis() - start)+" ms");
            done = true;
        }
        finally
        {
            if (!done)
            {
                newGenerationLog.deleteLog(this.getGeneratedDir(), xsdPath);
            }
        }
    }

    private void validateParams()
    {
        if (this.parserName == null)
        {
            throw new FreyaXmlException("Must specify parserName");
        }
    }

    private XsdSchema parseXsd() throws IOException
    {
        File file = new File(this.xsd);
        FileInputStream inputStream = new FileInputStream(file);
        FullFileBuffer ffb = new FullFileBuffer();
        ffb.bufferFile(inputStream, (int) file.length());
        inputStream.close();
        ffb.updateCrc(crc32);
        InputStream xsdFileIs = ffb.getBufferedInputStream();
        XsdSchema xsdSchema = XsdSchemaUnmarshaller.parse(xsdFileIs, "in file " + this.xsd, file.getParentFile().getAbsolutePath());
        long start = System.currentTimeMillis();
        String msg = this.xsd + ": parsed ";
        msg += " Parsed xsd in "+(System.currentTimeMillis() - start)+" ms.";
        this.logger.info(msg);
        List<String> errors = xsdSchema.validate();
        if (errors.size() != 0)
        {
            throw new FreyaXmlException("errors parsing xsd:"+toString(errors));
        }
        return xsdSchema;
    }

    private String toString(List<String> errors)
    {
        StringBuilder builder = new StringBuilder();
        for(String s: errors)
        {
            builder.append('\n').append(s);
        }
        return builder.toString();
    }

    private String concatParsed(String msg, int count, String type)
    {
        if (count > 0)
        {
            msg += count + " " +type+", ";
        }
        return msg;
    }

    private int getAvailableProcessors()
    {
        return Runtime.getRuntime().availableProcessors();
    }


    private void waitForExecutorWithCheck()
    {
        getExecutor().waitUntilDone();
        if (executorError != null)
        {
            throw new FreyaGeneratorException("exception while generating", executorError);
        }
    }

    private void printErrors(List errors)
    {
        for (int i = 0; i < errors.size(); i++)
        {
            this.logger.error("\t" + errors.get(i));
        }
    }

    protected void copyIfChanged(byte[] src, File outFile, AtomicInteger count) throws IOException, FreyaGeneratorException
    {
        boolean copyFile = false;
        if ((!outFile.exists()) || (outFile.length() != src.length))
        {
            copyFile = true;
        }
        else
        {
            byte[] outContent = readFile(outFile);
            for(int i=0;i<src.length;i++)
            {
                if (src[i] != outContent[i])
                {
                    copyFile = true;
                    break;
                }
            }
        }
        if (copyFile && outFile.exists() && !outFile.canWrite())
        {
            throw new FreyaGeneratorException(outFile+" must be updated, but it is readonly.");
        }

        if (copyFile)
        {
            FileOutputStream fout = new FileOutputStream(outFile);
            fout.write(src);
            fout.close();
            count.incrementAndGet();
            logger.debug("wrote file: " + outFile.getName());
        }
    }

    private byte[] readFile(File file) throws IOException
    {
        int length = (int)file.length();
        FileInputStream fis = new FileInputStream(file);
        byte[] result = new byte[length];
        int pos = 0;
        while(pos < length)
        {
            pos += fis.read(result, pos, length - pos);
        }
        fis.close();
        return result;
    }


    public StringBuilder getErrorLogs()
    {
        return errorLogs;
    }

    public ChopAndStickResource getChopAndStickResource()
    {
        return chopAndStickResource;
    }

    public AwaitingThreadExecutor getExecutor()
    {
        if (executor == null)
        {
            executor = new AwaitingThreadExecutor(Runtime.getRuntime().availableProcessors()+IO_THREADS, "Xsd2bean Generator");
            executor.setExceptionHandler(new AutoShutdownThreadExecutor.ExceptionHandler() {
                public void handleException(AutoShutdownThreadExecutor executor, Runnable target, Throwable exception)
                {
                    executor.shutdownNow();
                    logger.error("Error in runnable target. Shutting down queue "+exception.getClass().getName()+" :"+exception.getMessage(), exception);
                    executorError = exception;
                }
            });
        }
        return executor;
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    protected abstract class GeneratorTask implements Runnable
    {
        private int resourceNumber;

        protected GeneratorTask(int resourceNumber)
        {
            this.resourceNumber = resourceNumber;
        }

        public void acquireSerialResource()
        {
            chopAndStickResource.acquireSerialResource(resourceNumber);
        }

        public void releaseSerialResource()
        {
            chopAndStickResource.releaseSerialResource();
        }
    }

    private static class SharedXsdType implements GeneratorType
    {
        private String className;

        private SharedXsdType(String className)
        {
            this.className = className;
        }

        @Override
        public String getJavaTypeName()
        {
            return className;
        }

        @Override
        public String getTemplatePackage()
        {
            return "shared";
        }

        @Override
        public String getSubPackage()
        {
            return "";
        }
    }

}
