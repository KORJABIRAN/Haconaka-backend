package com.haconaka.demo.service.youtube;

import com.haconaka.demo.dto.PubSubNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class XmlParsingService {

    // XML 파싱 (Live INSERT 전 단계 이기도 함)
    public List<PubSubNotificationDto> parseAtomXml(String atomXml) throws Exception {
        List<PubSubNotificationDto> result = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        try (StringReader reader = new StringReader(atomXml)) {
            Document doc = builder.parse(new InputSource(reader));
            Element root = doc.getDocumentElement();

            NodeList entryNodes = root.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "entry");
            if (entryNodes.getLength() == 0) {
                entryNodes = root.getElementsByTagName("entry");
            }

            for (int i = 0; i < entryNodes.getLength(); i++) {
                Element entry = (Element) entryNodes.item(i);

                String videoId = getFirstTextContent(entry,
                        "http://www.youtube.com/xml/schemas/2015", "videoId");
                String channelId = getFirstTextContent(entry,
                        "http://www.youtube.com/xml/schemas/2015", "channelId");
                String title = getFirstTextContent(entry,
                        "http://www.w3.org/2005/Atom", "title");
                String published = getFirstTextContent(entry,
                        "http://www.w3.org/2005/Atom", "published");

                result.add(PubSubNotificationDto.builder()
                        .videoId(videoId)
                        .channelId(channelId)
                        .title(title)
                        .publishedAt(published)
                        .build());
            }
        }
        return result;
    }

    // XML 파싱 부품
    private String getFirstTextContent(Element parent, String ns, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(ns, localName);
        if (nodes.getLength() == 0) {
            nodes = parent.getElementsByTagName(localName);
            if (nodes.getLength() == 0) {
                return null;
            }
        }
        return nodes.item(0).getTextContent();
    }

}
