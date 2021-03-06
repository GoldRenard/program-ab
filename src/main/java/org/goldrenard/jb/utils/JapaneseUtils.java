/*
 * This file is part of Program JB.
 *
 * Program JB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Program JB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Program JB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.goldrenard.jb.utils;

import net.reduls.sanmoku.Tagger;
import org.goldrenard.jb.core.AIMLProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.stream.Collectors;

public class JapaneseUtils {

    private static final Logger log = LoggerFactory.getLogger(JapaneseUtils.class);

    /**
     * Tokenize a fragment of the input that contains only text
     *
     * @param fragment fragment of input containing only text and no XML tags
     * @return tokenized fragment
     */
    private static String tokenizeFragment(String fragment) {
        if (log.isDebugEnabled()) {
            log.debug("buildFragment: {}", fragment);
        }
        return Tagger.parse(fragment).stream().map(e -> e.surface).collect(Collectors.joining(" "));
    }

    /**
     * Morphological analysis of an input sentence that contains an AIML pattern.
     *
     * @param sentence
     * @return morphed sentence with one space between words, preserving XML markup and AIML $ operation
     */
    public static String tokenizeSentence(String sentence) {
        if (log.isDebugEnabled()) {
            log.debug("tokenizeSentence: {}", sentence);
        }
        String result = "";
        result = tokenizeXML(sentence);
        if (result != null) {
            while (result.contains("$ ")) result = result.replace("$ ", "$");
            while (result.contains("  ")) result = result.replace("  ", " ");
            while (result.contains("anon ")) result = result.replace("anon ", "anon"); // for Triple Store
            result = result.trim();
            if (log.isTraceEnabled()) {
                log.trace("tokenizeSentence: {} --> result: {}", sentence, result);
            }
        }
        return result;
    }

    private static String tokenizeXML(String xmlExpression) {
        if (log.isDebugEnabled()) {
            log.debug("tokenizeXML: {}", xmlExpression);
        }
        String response = "";
        try {
            xmlExpression = "<sentence>" + xmlExpression + "</sentence>";
            Node root = DomUtils.parseString(xmlExpression);
            response = recursEval(root);
        } catch (Exception e) {
            log.error("Error:", e);
        }
        return AIMLProcessor.trimTag(response, "sentence");
    }

    private static String recursEval(Node node) {
        try {
            String nodeName = node.getNodeName();
            if (log.isDebugEnabled()) {
                log.debug("recursEval: {}", nodeName);
            }
            switch (nodeName) {
                case "#text":
                    return tokenizeFragment(node.getNodeValue());
                case "sentence":
                    return evalTagContent(node);
                default:
                    return genericXML(node);
            }
        } catch (Exception e) {
            log.debug("recursEval failed", e);
        }
        return "JP Morph Error";
    }

    private static String genericXML(Node node) {
        if (log.isDebugEnabled()) {
            log.debug("genericXML: {}", node.getNodeName());
        }
        String result = evalTagContent(node);
        return unevaluatedXML(result, node);
    }

    private static String evalTagContent(Node node) {
        if (log.isDebugEnabled()) {
            log.debug("evalTagContent: {}", node.getNodeName());
        }
        StringBuilder result = new StringBuilder();
        try {
            NodeList childList = node.getChildNodes();
            for (int i = 0; i < childList.getLength(); i++) {
                Node child = childList.item(i);
                result.append(recursEval(child));
            }
        } catch (Exception e) {
            log.warn("Something went wrong with evalTagContent", e);
        }
        return result.toString();
    }

    private static String unevaluatedXML(String result, Node node) {
        String nodeName = node.getNodeName();
        StringBuilder attributes = new StringBuilder();
        if (node.hasAttributes()) {
            NamedNodeMap XMLAttributes = node.getAttributes();
            for (int i = 0; i < XMLAttributes.getLength(); i++) {
                attributes
                        .append(" ")
                        .append(XMLAttributes.item(i).getNodeName())
                        .append("=\"")
                        .append(XMLAttributes.item(i).getNodeValue())
                        .append("\"");
            }
        }
        if ("".equals(result)) {
            return " <" + nodeName + attributes + "/> ";
        } else {
            return " <" + nodeName + attributes + ">" + result + "</" + nodeName + "> ";   // add spaces
        }
    }
}
