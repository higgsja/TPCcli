package com.hpi.ofxFileHandling;

import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 *
 * @author blue
 */
public class SGMLNode
{

  private static final StringBuilder sbFix;
  private List<SGMLNode> nodeList;
  private String sOwnText;
  private Element elementSGMLNode;
  private SGMLNode parentSGMLNode;

  static
  {
	sbFix = new StringBuilder();
  }

  /**
   * Constructor
   */
  public SGMLNode()
  {
	//sbFix = new StringBuilder();
	nodeList = new ArrayList<>();
	sOwnText = "";
	elementSGMLNode = null;
	parentSGMLNode = null;
  }

  /**
   * Write a given node
   *
   * @return
   */
  public Boolean writeSGMLNode()
  {
	sbFix.append("<");
	sbFix.append(elementSGMLNode.tagName());
	sbFix.append(">\r\n");

	if (!sOwnText.equals(""))
	{
	  sbFix.append(sOwnText);
	  sbFix.append("\r\n");
	}
	for (SGMLNode aSGMLNode : nodeList)
	{
	  if (!aSGMLNode.writeSGMLNode())
	  {
		return false;
	  }
	}

	sbFix.append("</");
	sbFix.append(elementSGMLNode.tagName());
	sbFix.append(">\r\n");
	nodeList.clear();
	return true;
  }

  /**
   * Read a given node
   *
   * @return
   */
  public Boolean readSGMLNode()
  {
	Iterator<Element> iterator;
	Element e1;
	setsOwnText(elementSGMLNode.ownText());

	iterator = elementSGMLNode.children().iterator();
	while (iterator.hasNext())
	{
	  e1 = iterator.next();

	  if (elementSGMLNode.ownText().equals(""))
	  {
		// this has no text
		if (e1.ownText().equals(""))
		{
		  // this has no text, the child has no text
		  // could be spurious <code>
		  if (e1.tagName().equals("code"))
		  {
			// spurious <code>, make child of this
			String s2 = e1.toString();
			// Take the right part without <code>
			s2 = s2.substring(7, s2.length() - 7);
			e1 = Jsoup.parseBodyFragment(s2);

			Iterator<Element> iterator1;// = e1.select("body").iterator();
			iterator1 = e1.child(0).child(1).children().iterator();

			while (iterator1.hasNext())
			{
			  Element e2;
			  e2 = iterator1.next();

			  // there is a case where e2 is a spurious <code>
			  if (e2.tagName().equals("code") && e2.ownText().equals(""))
			  {
				String s3 = e2.toString();
				s3 = s3.substring(7, s3.length() - 7);
				e2 = Jsoup.parseBodyFragment(s3);

				Iterator<Element> iterator2;
				iterator2 = e2.child(0).child(1).children().iterator();

				while (iterator2.hasNext())
				{
				  Element e3;
				  e3 = iterator2.next();

				  SGMLNode workSGMLNode = new SGMLNode();
				  workSGMLNode.setElementSGMLNode(e3);

				  workSGMLNode.setParentSGMLNode(this);
				  nodeList.add(workSGMLNode);

				  workSGMLNode.readSGMLNode();
				}
			  }
			  else
			  {
				SGMLNode workSGMLNode = new SGMLNode();
				workSGMLNode.setElementSGMLNode(e2);
				workSGMLNode.setParentSGMLNode(this);
				nodeList.add(workSGMLNode);

				workSGMLNode.readSGMLNode();
			  }
			}
		  }
		  else
		  {
			// not spurious <code>, make it a child of this
			SGMLNode workSGMLNode = new SGMLNode();
			workSGMLNode.setElementSGMLNode(e1);
			workSGMLNode.setParentSGMLNode(this);
			nodeList.add(workSGMLNode);

			workSGMLNode.readSGMLNode();
		  }
		}
		else
		{
		  // this has no text, the child has text
		  // make it a child of this
		  SGMLNode workSGMLNode = new SGMLNode();
		  workSGMLNode.setElementSGMLNode(e1);
		  workSGMLNode.setParentSGMLNode(this);
		  nodeList.add(workSGMLNode);

		  workSGMLNode.readSGMLNode();
		}
	  }
	  else
	  {
		if (e1.ownText().equals(""))
		{
		  // this has text, child does not
		  // make child of parent
		  // could be spurious <code>
		  // Hit? YES
		  if (e1.tagName().equals("code"))
		  {
			// spurious <code>, make child of this
			String s2 = e1.toString();
			// Take the right part without <code>
			s2 = s2.substring(7, s2.length() - 7);
			e1 = Jsoup.parseBodyFragment(s2);

			Iterator<Element> iterator1;// = e1.select("body").iterator();
			iterator1 = e1.child(0).child(1).children().iterator();

			while (iterator1.hasNext())
			{
			  Element e2;
			  e2 = iterator1.next();

			  SGMLNode workSGMLNode = new SGMLNode();
			  workSGMLNode.setElementSGMLNode(e2);
			  workSGMLNode.setParentSGMLNode(getParentSGMLNode());
			  getParentSGMLNode().getSGMLNodeList().add(workSGMLNode);

			  workSGMLNode.readSGMLNode();
			}
		  }
		  else
		  {
			// not spurious <code>
			SGMLNode workSGMLNode = new SGMLNode();
			workSGMLNode.setElementSGMLNode(e1);
			workSGMLNode.setParentSGMLNode(getParentSGMLNode());
			getParentSGMLNode().getSGMLNodeList().add(workSGMLNode);

			workSGMLNode.readSGMLNode();
		  }
		}
		else
		{
		  // this has text, child has text
		  // make child of parent (series of data elements)
		  SGMLNode workSGMLNode = new SGMLNode();
		  workSGMLNode.setElementSGMLNode(e1);
		  workSGMLNode.setParentSGMLNode(getParentSGMLNode());
		  getParentSGMLNode().getSGMLNodeList().add(workSGMLNode);

		  workSGMLNode.readSGMLNode();
		}
	  }
	}
	return true;
  }

  /**
   * Get the node list
   *
   * @return
   */
  public List<SGMLNode> getSGMLNodeList()
  {
	return nodeList;
  }

  /**
   * Set the node list
   *
   * @param nodeList
   */
  public void setSGMLNodeList(List<SGMLNode> nodeList)
  {
	this.nodeList = nodeList;
  }

  /**
   * Get element at node
   *
   * @return
   */
  public Element getElementSGMLNode()
  {
	return elementSGMLNode;
  }

  /**
   * Set element at node
   *
   * @param elementSGMLNode
   */
  public void setElementSGMLNode(Element elementSGMLNode)
  {
	this.elementSGMLNode = elementSGMLNode;
  }

  /**
   * Get parent at given node
   *
   * @return
   */
  public SGMLNode getParentSGMLNode()
  {
	return parentSGMLNode;
  }

  /**
   * set parent for given node
   *
   * @param parentSGMLNode
   */
  public void setParentSGMLNode(SGMLNode parentSGMLNode)
  {
	this.parentSGMLNode = parentSGMLNode;
  }

  /**
   * get text for given node
   *
   * @return
   */
  public String getsOwnText()
  {
	return sOwnText;
  }

  /**
   * Set text for given node
   *
   * @param sOwnText
   */
  public void setsOwnText(String sOwnText)
  {
	this.sOwnText = sOwnText;
  }

  /**
   * get node list
   *
   * @return
   */
  public List<SGMLNode> getNodeList()
  {
	return nodeList;
  }

  /**
   *
   * @param nodeList
   */
  public void setNodeList(List<SGMLNode> nodeList)
  {
	this.nodeList = nodeList;
  }

  /**
   *
   * @return
   */
  public StringBuilder getSbFix()
  {
	return sbFix;
  }
}
