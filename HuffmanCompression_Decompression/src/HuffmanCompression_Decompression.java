//File: HuffmanCompression.java
//Author: Jonathan Carpenter
//Date: 4/28/2016
//Description: Implements the Huffman Compression and Decompression algorithms
//             described in the book:  Pu, Ida M. Fundamental data compression.
//             Oxford Burlington, MA: Butterworth-Heinemann, 2006. Print.
//             Section 4.1


import java.util.*;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.File;

/**
 * Class used to compress and decompress strings using huffman compression
 */
public class HuffmanCompression_Decompression
{
    private HashMap<String, Node> map;
    private TreeMap<String, Node> treemap;
    private HashMap<String, String> huffmanCodes;
    private HashMap<String, String> huffmanDecodes;
    private ByteArrayOutputStream ostream; 
    private BinaryStdOut Bout;
    private BinaryStdIn Bin;
    private Node huffmanTree;
    private String data;

    /**
     * Returns a HuffmanCompression object that can be used to compress and decompress strings
     */
    public HuffmanCompression_Decompression() {

	this.map = null;
	this.treemap = null;
	this.huffmanCodes = null;
	this.huffmanDecodes = null;
	this.huffmanTree = null;
	this.ostream = null;
	
    }

    /**
     * Returns a string of compressed data
     * @param  input  data to be compressed
     */
    public String compress(String input) {

	// If string to compress is empty return the empty string
	if(input == "") {

	    System.out.println("Nothing to compress");
	    System.exit(1);

	}

	// If string contains a negative number return the empty string
	if(input.contains("-")) {

	    System.out.println("Bad data: Contains negative number");
	    System.exit(2);

	}
	
	try
	    {
		ostream = new ByteArrayOutputStream();

		this.Bout = new BinaryStdOut(
					     new BufferedOutputStream(ostream));

		this.Bin = new BinaryStdIn(
					   new BufferedInputStream(
								   new ByteArrayInputStream(input.getBytes("ISO-8859-1"))));

		this.huffmanCodes = new HashMap<String, String>();
	
		getFrequencies();
		sortMapByValue();


		this.huffmanTree = buildHuffmanTree();
		createHuffmanEncodeMap(this.huffmanTree, "");
		writeTree(this.huffmanTree);
		encode();

	    
		return new String(ostream.toByteArray(), "ISO-8859-1");
	    }
	catch(Exception e) {
	    System.out.println("Error could not compress!!");
	    e.printStackTrace();
	}

	return "";
    }

    /**
     * Returns a decompressed string that was encoded with the compression method
     * @param  input        a compressed string
     * @see    compression  
     */
    public String decompress(String input) {
	try
	    {
		this.Bin = new BinaryStdIn(
			       new BufferedInputStream(
				   new ByteArrayInputStream(input.getBytes("ISO-8859-1"))));

		this.huffmanTree = readTree();
		this.huffmanDecodes = new HashMap<String, String>();
		createHuffmanDecodeMap(huffmanTree, "");

		String retval = new String(decode().toByteArray(), "ISO-8859-1");
		return retval;
	    }
		catch(Exception e){
		    System.out.println("Error could not compress!!");
		    e.printStackTrace();
		}

	return "";

    }

    /**
     * Encodes huffmanTree into a ByteArrayOutputStream
     * @param  node  Root of huffman tree 
     */
    private void writeTree(Node node) {
	if(node.leftChild == null && node.rightChild == null) {
	    Bout.write(true);
	    Bout.write(node.string.charAt(0), 8);

	}
	else {
	    Bout.write(false);
	    if(node.leftChild != null) { writeTree(node.leftChild);  }
	    if(node.rightChild != null){ writeTree(node.rightChild); }

	}
    }

    /**
     * Returns the root of a huffman tree read
     * from an encoded huffman tree from a ByteArrayInputStream.
     */
    private Node readTree() {

	if(Bin.readBoolean()) {
	    char c = Bin.readChar();
	    return new Node(Character.toString(c), 0);

	}
	else {
	    return new Node(readTree(), readTree());
	}
    }

    /**
     * Encodes a data into a ByteArrayOutputStream using the corresponding huffman codes
     */
    private void encode() {

	////////// TEST
	// System.out.println("");
	// System.out.println("huffman codes");
	// Set<Map.Entry<String, String>> set = huffmanCodes.entrySet();
	// Iterator<Map.Entry<String, String>> it = set.iterator();
	// while(it.hasNext()) {
	//     Map.Entry<String, String> entry = it.next(); 
	//     System.out.println(entry.getKey() + " " + entry.getValue());
	// }
	/////////

	long count = 0;

	for(int i=0;i<this.data.length();i++){
	    String code = huffmanCodes.get(Character.toString(this.data.charAt(i)));
	    count += code.length();
	}

	Bout.write(count);

	for(int i=0;i<this.data.length();i++) {

	    String code = huffmanCodes.get(Character.toString(this.data.charAt(i)));

	    for(int j=0;j<code.length();j++) {

		if(code.charAt(j) == '0'){
		    Bout.write(false);
		}
		else {
		    Bout.write(true);
		}
	    }

	}
	Bout.close();
    }

    /**
     * Returns a decoded array of bytes from a encoded string
     */
    private ByteArrayOutputStream decode() {

	ByteArrayOutputStream ostream = new ByteArrayOutputStream();
	this.Bout = new BinaryStdOut(
			new BufferedOutputStream(ostream));


	long numBits = Bin.readLong();
	String path = "";
	for(long i=0;i < numBits;i++) {

	    if(Bin.readBoolean()){
		path += "1";
	    }
	    else {
		path += "0";
	    }

	    if(huffmanDecodes.containsKey(path)) {

		String c = huffmanDecodes.get(path);
		Bout.write(c.charAt(0));
		path = "";

	    }
	}

	Bout.close();
	return ostream;
    }

    /**
     * Recursively builds a mapping of nodes to huffman codes 
     * @param  currentNode  current node in huffman tree
     * @param  path         current location in huffman tree
     */
    private void createHuffmanEncodeMap(Node currentNode, String path) {
	if(currentNode == null) {

	    return;

	}
	else {

	    if(currentNode.leftChild == null && currentNode.rightChild == null) {
		if(path == ""){ path = "1"; }
		this.huffmanCodes.put(currentNode.string, path);
	    }

	    createHuffmanEncodeMap(currentNode.leftChild, path + "0");
	    createHuffmanEncodeMap(currentNode.rightChild, path + "1");
	    
	}
    }

    /**
     * Recursively builds a mapping of huffman codes to nodes
     * @param  currentNode  current node in huffman tree
     * @param  path         current location in huffman tree
     */
    private void createHuffmanDecodeMap(Node currentNode, String path) {
	if(currentNode == null) {

	    return;

	}
	else {

	    if(currentNode.leftChild == null && currentNode.rightChild == null) {
		if(path == ""){ path = "1"; }
		this.huffmanDecodes.put(path, currentNode.string);
	    }

	    createHuffmanDecodeMap(currentNode.leftChild, path + "0");
	    createHuffmanDecodeMap(currentNode.rightChild, path + "1");
	    
	}
    }
    
    /**
     * Return the root of the huffman tree
     * 
     * Builds huffman tree according to frequency character frequency
     */
    private Node buildHuffmanTree() {

	Node node = null;

	if(map.size() == 1) {
	    node = treemap.firstEntry().getValue();
	}
	
	while(map.size() != 1) {

	    Map.Entry<String, Node> entry1 = treemap.pollFirstEntry();
	    Map.Entry<String, Node> entry2 = treemap.pollFirstEntry();
	    map.remove(entry1.getKey());
	    map.remove(entry2.getKey());

	    node = new Node(entry2.getValue(), entry1.getValue());
	    node.frequency = entry2.getValue().frequency + entry2.getValue().frequency;
	    node.string = entry2.getKey() + entry1.getKey();

	    map.put(node.string, node);
	    treemap.put(node.string, node);

	}

	return node;
    }


    /**
     * Copys the contents in a HashMap into a ordered Treemap according to frequency
     */
    private void sortMapByValue() {

	Comparator<String> comparator = new ValueComparator(this.map);
	treemap = new TreeMap<String, Node>(comparator);
	treemap.putAll(map);

    }

    /**
     *  Builds a mapping of characters to character frequencies
     */
    private void getFrequencies() {

	this.map = new HashMap<String, Node>();
	this.data = Bin.readString();
	
	for(int i=0;i<data.length();i++) {

	    String c = Character.toString(data.charAt(i));
	    if (map.containsKey(c)) {

		Node node = map.get(c);
		node.frequency++;
		map.put(c, node);		

	    }
	    else {
		map.put(c, new Node(c, 1));
	    }
	}
    }

    public static void main(String[] args) {

	String c;
	String d;
	
	HuffmanCompression_Decompression huffman = new HuffmanCompression_Decompression();
	c = huffman.compress("-jjjjjj");
	d = huffman.decompress(c);
	System.out.println("Compressed: " + c);
	System.out.println("Decompressed: " + d);

    }
}

/**
 * Comparator for elements of a Hashmap
 */
class ValueComparator implements Comparator<String>
{
    
    HashMap<String, Node> map;

    public ValueComparator(HashMap<String, Node> map) {
	this.map = map;
    }

    public int compare(String a, String b) {

	if (map.get(a).frequency <= map.get(b).frequency) {
	    return -1;
	}
	else {
	    return 1;
	}
    }
}

/**
 * Node for HuffmanCompression class
 */
class Node 
{
    int frequency;
    String string;
    Node leftChild;
    Node rightChild;

    public Node(String string, int frequency) {
	this.frequency = frequency;
	this.string = string;
	this.leftChild = null;
	this.rightChild = null;

    }

    public Node(Node lc, Node rc) {
	this.frequency = 0;
	this.string = "";
	this.leftChild = lc;
	this.rightChild = rc;
    }
}
