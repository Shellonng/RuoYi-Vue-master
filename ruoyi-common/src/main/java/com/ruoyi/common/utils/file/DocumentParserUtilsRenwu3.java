package com.ruoyi.common.utils.file;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DocumentParserUtilsRenwu3
{
    private static final Logger log = LoggerFactory.getLogger(DocumentParserUtilsRenwu3.class);

    public static String parseDocument(File file, String fileType) throws IOException
    {
        if (file == null || !file.exists())
        {
            throw new IOException("文件不存在");
        }

        String text = "";
        fileType = fileType.toLowerCase();

        switch (fileType)
        {
            case "pdf":
                text = parsePDF(file);
                break;
            case "doc":
                text = parseDoc(file);
                break;
            case "docx":
                text = parseDocx(file);
                break;
            default:
                throw new IOException("不支持的文件类型: " + fileType);
        }

        return text;
    }

    /**
     * 解析PDF文档
     * 
     * @param file PDF文件
     * @return 提取的文本
     */
    private static String parsePDF(File file) throws IOException
    {
        PDDocument document = null;
        try
        {
            document = PDDocument.load(file);
            PDFTextStripper stripper = new PDFTextStripper();
            
            // 设置提取范围（可选，这里提取所有页）
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            
            String text = stripper.getText(document);
            log.info("PDF解析成功，共{}页，提取文本长度: {}", document.getNumberOfPages(), text.length());
            
            return text;
        }
        catch (IOException e)
        {
            log.error("PDF解析失败: {}", e.getMessage());
            throw e;
        }
        finally
        {
            if (document != null)
            {
                try
                {
                    document.close();
                }
                catch (IOException e)
                {
                    log.error("关闭PDF文档失败", e);
                }
            }
        }
    }

    /**
     * 解析旧版Word文档（.doc）
     * 
     * @param file Word文件
     * @return 提取的文本
     */
    private static String parseDoc(File file) throws IOException
    {
        FileInputStream fis = null;
        HWPFDocument document = null;
        WordExtractor extractor = null;
        
        try
        {
            fis = new FileInputStream(file);
            document = new HWPFDocument(fis);
            extractor = new WordExtractor(document);
            
            String text = extractor.getText();
            log.info("Word(.doc)解析成功，提取文本长度: {}", text.length());
            
            return text;
        }
        catch (IOException e)
        {
            log.error("Word(.doc)解析失败: {}", e.getMessage());
            throw e;
        }
        finally
        {
            if (extractor != null)
            {
                try
                {
                    extractor.close();
                }
                catch (IOException e)
                {
                    log.error("关闭Word提取器失败", e);
                }
            }
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                    log.error("关闭文件流失败", e);
                }
            }
        }
    }

    /**
     * 解析新版Word文档（.docx）
     * 
     * @param file Word文件
     * @return 提取的文本
     */
    private static String parseDocx(File file) throws IOException
    {
        FileInputStream fis = null;
        XWPFDocument document = null;
        XWPFWordExtractor extractor = null;
        
        try
        {
            fis = new FileInputStream(file);
            document = new XWPFDocument(fis);
            extractor = new XWPFWordExtractor(document);
            
            String text = extractor.getText();
            log.info("Word(.docx)解析成功，提取文本长度: {}", text.length());
            
            return text;
        }
        catch (IOException e)
        {
            log.error("Word(.docx)解析失败: {}", e.getMessage());
            throw e;
        }
        finally
        {
            if (extractor != null)
            {
                try
                {
                    extractor.close();
                }
                catch (IOException e)
                {
                    log.error("关闭Word提取器失败", e);
                }
            }
            if (document != null)
            {
                try
                {
                    document.close();
                }
                catch (IOException e)
                {
                    log.error("关闭Word文档失败", e);
                }
            }
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                    log.error("关闭文件流失败", e);
                }
            }
        }
    }

    /**
     * 从输入流解析PDF
     * 
     * @param inputStream 输入流
     * @return 提取的文本
     */
    public static String parsePDFFromStream(InputStream inputStream) throws IOException
    {
        PDDocument document = null;
        try
        {
            document = PDDocument.load(inputStream);
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
        finally
        {
            if (document != null)
            {
                document.close();
            }
        }
    }

    /**
     * 从输入流解析Word文档(.docx)
     * 
     * @param inputStream 输入流
     * @return 提取的文本
     */
    public static String parseDocxFromStream(InputStream inputStream) throws IOException
    {
        XWPFDocument document = null;
        XWPFWordExtractor extractor = null;
        
        try
        {
            document = new XWPFDocument(inputStream);
            extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
        finally
        {
            if (extractor != null)
            {
                extractor.close();
            }
            if (document != null)
            {
                document.close();
            }
        }
    }
}
