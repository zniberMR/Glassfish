/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.oxm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.persistence.internal.oxm.XPathFragment;
import org.eclipse.persistence.internal.oxm.record.MarshalContext;
import org.eclipse.persistence.internal.oxm.record.ObjectMarshalContext;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.persistence.oxm.record.MarshalRecord;

/**
 * INTERNAL:
 * <p><b>Purpose</b>:  XPathNodes are used together to build a tree.  The tree
 * is built from all of the XPath statements specified in the mapping metadata 
 * (mappings and policies).  This tree is then navigated by the 
 * TreeObjectBuilder to perform marshal and unmarshal operations.</p>
 * <p>The XPaths "a/b" and "a/c" would result in a tree with the root "a" and 
 * two child nodes "b" and "c".</p>
 * <p><b>Responsibilities</b>:<ul>
 * <li>All tree relationships must be bi-directional.</li>
 * <li>Reference a NodeValue, XPathNodes without a Node value represent grouping
 * elements.</li>
 * <li>Reference an XPathFragment, XPathFragments contain name and namespace
 * information.</li>
 * <li>Must differentiate between child nodes that correspond to elements and 
 * those that do not.</li>
 * <li>Must represent special mapping situations like any and self mappings.</li>
 * </ul> 
 */

public class XPathNode {
    private NodeValue unmarshalNodeValue;
    private NodeValue marshalNodeValue;
    private XPathFragment xPathFragment;
    private XPathNode parent;
    private List<XPathNode> attributeChildren;
    private List<XPathNode> nonAttributeChildren;
    private List selfChildren;
    private Map<XPathFragment, XPathNode> attributeChildrenMap;
    private Map<XPathFragment, XPathNode> nonAttributeChildrenMap;
    private XMLAnyAttributeMappingNodeValue anyAttributeNodeValue;
    private XPathNode anyAttributeNode;
    private XPathNode textNode;
    private XPathNode anyNode;

    public XPathFragment getXPathFragment() {
        return xPathFragment;
    }

    public void setXPathFragment(XPathFragment xPathFragment) {
        this.xPathFragment = xPathFragment;
    }

    public NodeValue getNodeValue() {
        return unmarshalNodeValue;
    }

    public void setNodeValue(NodeValue nodeValue) {
        this.marshalNodeValue = nodeValue;
        this.unmarshalNodeValue = nodeValue;
        if (null != nodeValue) {
            nodeValue.setXPathNode(this);
        }
    }
    
    public NodeValue getUnmarshalNodeValue() {
        return unmarshalNodeValue;
    }
    
    public void setUnmarshalNodeValue(NodeValue nodeValue) {
        if (null != nodeValue) {
            nodeValue.setXPathNode(this);
        }
        this.unmarshalNodeValue = nodeValue;
    }

    public NodeValue getMarshalNodeValue() {
        return marshalNodeValue;
    }
    
    public void setMarshalNodeValue(NodeValue nodeValue) {
        if (null != nodeValue) {
            nodeValue.setXPathNode(this);
        }
        this.marshalNodeValue = nodeValue;
    }
    
    public XPathNode getParent() {
        return parent;
    }

    public void setParent(XPathNode parent) {
        this.parent = parent;
    }

    public List<XPathNode> getAttributeChildren() {
        return this.attributeChildren;
    }

    public List<XPathNode> getNonAttributeChildren() {
        return this.nonAttributeChildren;
    }

    public List<XPathNode> getSelfChildren() {
        return this.selfChildren;
    }

    public Map<XPathFragment, XPathNode> getNonAttributeChildrenMap() {
        return this.nonAttributeChildrenMap;
    }

    public Map<XPathFragment, XPathNode> getAttributeChildrenMap() {
        return this.attributeChildrenMap;
    }

    public void setAnyAttributeNodeValue(XMLAnyAttributeMappingNodeValue nodeValue) {
        this.anyAttributeNodeValue = nodeValue;
    }

    public XMLAnyAttributeMappingNodeValue getAnyAttributeNodeValue() {
        return this.anyAttributeNodeValue;
    }

    public XPathNode getAnyAttributeNode() {
        return this.anyAttributeNode;
    }

    public XPathNode getAnyNode() {
        return this.anyNode;
    }

    public void setAnyNode(XPathNode xPathNode) {
        this.anyNode = xPathNode;
    }

    public XPathNode getTextNode() {
        return this.textNode;
    }

    public void setTextNode(XPathNode xPathNode) {
        this.textNode = xPathNode;
    }

    public boolean equals(Object object) {
        try {
            XPathNode perfNode = (XPathNode)object;

            if ((getXPathFragment() == null) && (perfNode.getXPathFragment() != null)) {
                return false;
            }
            if ((getXPathFragment() != null) && (perfNode.getXPathFragment() == null)) {
                return false;
            }
            if (getXPathFragment() == perfNode.getXPathFragment()) {
                return true;
            }
            return this.getXPathFragment().equals(perfNode.getXPathFragment());

            // turn fix off for now until we re-enable XMLAnyObjectAndAnyCollectionTestCases
            //          } catch (NullPointerException npe) {
            // b5259059 all cases X0X1 (1mapping xpath=null, 2nd mapping xpath=filled
            // catch when object.getXPathFragment() == null
            // (this will also catch case where perfNode XPath is null)
            //          	return false;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public XPathNode addChild(XPathFragment anXPathFragment, NodeValue aNodeValue, NamespaceResolver namespaceResolver) {
        if (null != anXPathFragment && anXPathFragment.nameIsText()) {
            if (aNodeValue.isOwningNode(anXPathFragment)) {
                XPathNode textXPathNode = new XPathNode();
                textXPathNode.setParent(this);
                textXPathNode.setXPathFragment(anXPathFragment);
                if (aNodeValue.isMarshalNodeValue()) {
                    textXPathNode.setMarshalNodeValue(aNodeValue);
                }
                if (aNodeValue.isUnmarshalNodeValue()) {
                    textXPathNode.setUnmarshalNodeValue(aNodeValue);
                }
                this.setTextNode(textXPathNode);
                if (null == nonAttributeChildren) {
                    nonAttributeChildren = new ArrayList();
                }
                nonAttributeChildren.add(textXPathNode);
                return textXPathNode;
            }
        }

        if (anXPathFragment != null && namespaceResolver != null && anXPathFragment.getNamespaceURI() == null && !anXPathFragment.nameIsText()) {
            if(!anXPathFragment.isAttribute()) {
                anXPathFragment.setNamespaceURI(namespaceResolver.resolveNamespacePrefix(anXPathFragment.getPrefix()));
            } else if(anXPathFragment.hasNamespace()) {
                anXPathFragment.setNamespaceURI(namespaceResolver.resolveNamespacePrefix(anXPathFragment.getPrefix()));
            }
        }

        XPathNode xPathNode = new XPathNode();
        xPathNode.setXPathFragment(anXPathFragment);

        List children;
        Map childrenMap;

        if ((anXPathFragment != null) && anXPathFragment.isAttribute()) {
            if (null == attributeChildren) {
                attributeChildren = new ArrayList();
            }
            if (null == attributeChildrenMap) {
                attributeChildrenMap = new HashMap();
            }
            children = attributeChildren;
            childrenMap = attributeChildrenMap;
        } else {
            if (null == nonAttributeChildren) {
                nonAttributeChildren = new ArrayList();
            }
            if (null == nonAttributeChildrenMap) {
                nonAttributeChildrenMap = new HashMap();
            }
            children = nonAttributeChildren;
            childrenMap = nonAttributeChildrenMap;
        }

        if (null == anXPathFragment) {
            if(aNodeValue.isMarshalNodeValue()) {
                xPathNode.setMarshalNodeValue(aNodeValue);
            }
            if(aNodeValue.isUnmarshalNodeValue() && xPathNode.getUnmarshalNodeValue() == null) {
                xPathNode.setUnmarshalNodeValue(aNodeValue);
            }
            xPathNode.setParent(this);
            if (aNodeValue instanceof XMLAnyAttributeMappingNodeValue) {
                setAnyAttributeNodeValue((XMLAnyAttributeMappingNodeValue)aNodeValue);
                anyAttributeNode = xPathNode;
            } else {
                if(!children.contains(xPathNode)) {
                    children.add(xPathNode);
                }
                setAnyNode(xPathNode);
            }
            return xPathNode;
        }
        boolean isSelfFragment = XPathFragment.SELF_FRAGMENT.equals(anXPathFragment);

        if(isSelfFragment){
            children.add(xPathNode);
            if (null == selfChildren) {
                selfChildren = new ArrayList();
            }
            selfChildren.add(xPathNode);
        }else{
            int index = children.indexOf(xPathNode);
            if (index >= 0) {
                xPathNode = (XPathNode)children.get(index);
            } else {
                xPathNode.setParent(this);
                if(!children.contains(xPathNode)) {
                    children.add(xPathNode);
                }
                childrenMap.put(anXPathFragment, xPathNode);
            }
        }

        if (aNodeValue.isOwningNode(anXPathFragment)) {
            if(aNodeValue.isMarshalNodeValue()) {
                xPathNode.setMarshalNodeValue(aNodeValue);
            } 
            if(aNodeValue.isUnmarshalNodeValue() && xPathNode.getUnmarshalNodeValue() == null) {
                xPathNode.setUnmarshalNodeValue(aNodeValue);
            }
        } else {
            XPathFragment nextFragment = anXPathFragment.getNextFragment();
            xPathNode.addChild(nextFragment, aNodeValue, namespaceResolver);
        }
        return xPathNode;
    }

    public boolean marshal(MarshalRecord marshalRecord, Object object, AbstractSession session, NamespaceResolver namespaceResolver, XMLMarshaller marshaller, MarshalContext marshalContext) {
        if ((null == marshalNodeValue) || marshalNodeValue.isMarshalOnlyNodeValue()) {
            marshalRecord.addGroupingElement(this);

            boolean hasValue = false;
            if (null != attributeChildren) {
                for (int x = 0, size = attributeChildren.size(); x < size; x++) {
                    XPathNode xPathNode = attributeChildren.get(x);
                    hasValue = xPathNode.marshal(marshalRecord, object, session, namespaceResolver, marshaller, ObjectMarshalContext.getInstance()) || hasValue;
                }
            }
            if (anyAttributeNode != null) {
                hasValue = anyAttributeNode.marshal(marshalRecord, object, session, namespaceResolver, marshaller, ObjectMarshalContext.getInstance()) || hasValue;
            }
            if (null != nonAttributeChildren) {
                for (int x = 0, size = marshalContext.getNonAttributeChildrenSize(this); x < size; x++) {
                    XPathNode xPathNode = (XPathNode)marshalContext.getNonAttributeChild(x, this);
                    MarshalContext childMarshalContext = marshalContext.getMarshalContext(x);
                    hasValue = xPathNode.marshal(marshalRecord, object, session, namespaceResolver, marshaller, childMarshalContext) || hasValue;
                }
            }

            if (hasValue) {
                marshalRecord.endElement(xPathFragment, namespaceResolver);
            } else {
                marshalRecord.removeGroupingElement(this);
            }

            return hasValue;
        } else {
            return marshalContext.marshal(marshalNodeValue, xPathFragment, marshalRecord, object, session, namespaceResolver);
        }
    }

    public boolean startElement(MarshalRecord marshalRecord, XPathFragment anXPathFragment, Object object, AbstractSession session, NamespaceResolver namespaceResolver, TreeObjectBuilder compositeObjectBuilder, Object compositeObject) {
        if (null == anXPathFragment) {
            return false;
        }
        marshalRecord.openStartElement(anXPathFragment, namespaceResolver);
        boolean hasValue = false;
        if (null != attributeChildren) {
            for (int x = 0, size = attributeChildren.size(); x < size; x++) {
                XPathNode attributeNode = (XPathNode)attributeChildren.get(x);
                hasValue = attributeNode.marshal(marshalRecord, object, session, namespaceResolver, null, ObjectMarshalContext.getInstance()) || hasValue;
            }
        }
        if (anyAttributeNode != null) {
            //marshal the anyAttribute node here before closeStartElement()
            hasValue = anyAttributeNode.marshal(marshalRecord, object, session, namespaceResolver, null, ObjectMarshalContext.getInstance()) || hasValue;
        }

        if (null != compositeObjectBuilder) {
            hasValue = compositeObjectBuilder.marshalAttributes(marshalRecord, compositeObject, session) || hasValue;
        }
        marshalRecord.closeStartElement();
        return hasValue;
    }

    /**
     * Marshal any 'self' mapped attributes.  
     * 
     * @param marshalRecord
     * @param object
     * @param session
     * @param namespaceResolver
     * @param marshaller
     * @return
     */
    public boolean marshalSelfAttributes(MarshalRecord marshalRecord, Object object, AbstractSession session, NamespaceResolver namespaceResolver, XMLMarshaller marshaller) {
        if (marshalNodeValue == null) {
            return false;
        }
        return marshalNodeValue.marshalSelfAttributes(xPathFragment, marshalRecord, object, session, namespaceResolver, marshaller);
    }

    public boolean isWhitespaceAware() {
        return this.getNodeValue().isWhitespaceAware();
    }

}
