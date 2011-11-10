package ar.edu.itba.pod.legajo48421.multithread;

import ar.edu.itba.node.NodeInformation;

public class MsgError {

	
	
	public static String CONNECTION_ERROR = "Problems with connection";
	
	public static String connectionError(NodeInformation node){
		return MsgError.CONNECTION_ERROR + "Host: " + node.host() + " Port " + node.port();
	}
	
}
