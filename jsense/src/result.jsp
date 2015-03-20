<%@ page language="java" import="in.ac.iiitd.pag.matcher.*" %>
<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="java.io.*" %>	
<%@ page language="java" import="in.ac.iiitd.pag.util.*" %>	
<table width=100% height=98%>
 <tr>
 	<td align=center valign=top>
    	<table width=800px cellpadding=0 cellspacing=0 border=0>
    		<tr>
    			<td colspan=2><img src=jsenselogo.png height=100px/>    
    				<hr color=brown>				
    			</td>
    		</tr>
<%
	String code1=request.getParameter("code1");

	if (code1 == null) code1 = "";

    boolean somethingToDo = false;
    
	if ((code1.trim().length() > 0))	{	
		somethingToDo = true;
		
	}
	
if (!somethingToDo) { code1 = "No input given. Please give some input program.";}
		
%>    
    		<tr valign=top>
    			<td valign=top><b>Input:</b>
    				<pre>
<%= code1 %>
    				</pre>
    			</td>
    			<td valign=middle>
<%  if (somethingToDo) {
		/*
		*/
		try {
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/WEB-INF/lenovo-THINK.properties"));
		String luceneIndexFilePath = properties.getProperty("ALGO_REPO_INDEX_FILE_PATH");
				
		InputStream i = getServletContext().getResourceAsStream("/WEB-INF/javastops.txt");
		List<String> stops = FileUtil.readFromFileAsList(i);		
		i.close();
		i = getServletContext().getResourceAsStream("/WEB-INF/prefixStops.txt");		
		List<String> prefixStops = FileUtil.readFromFileAsList(i);	
        i.close();
        i = getServletContext().getResourceAsStream("/WEB-INF/canonicalizedOperators.txt");		
		List<String> operators = FileUtil.readFromFileAsList(i);	
        i.close();
        MatchResults mrs = Matcher.match(luceneIndexFilePath, code1, operators, stops, prefixStops,4,10);
		for(String topic: mrs.topicWiseResults.keySet()) {
			List<MatchResult> mrList = mrs.topicWiseResults.get(topic);
			out.println("<b><div align=center style=\"background-color:black;color:white;width:200;text-align:center\">Found Topic: " + topic + "</div></b><hr>");
			/* for(MatchResult mr: mrList) {
				out.println("<b>Matching Implementation: <b><pre>");
				out.println("(In stackoverflow post " + mr.postId + ": " + mr.title + ")");
				out.println(mr.snippet+ "</pre>");			
			}*/
			
			List<MatchResult> mrListAll =  LuceneUtil.getAllVariants(luceneIndexFilePath, topic);
			for(MatchResult mr: mrListAll) {
				out.println("<b>Variant Implementation: <b><pre>");
				out.println("(In stackoverflow post " + mr.postId + ": " + mr.title + ")");
				out.println(mr.snippet+ "</pre>");
			}
		}
		/*out.println(luceneIndexFilePath);
		out.println(code1);
		out.println(stops.size());
		out.println(prefixStops.size());
		out.println(operators.size()); */
		//out.println(output);
		if (mrs.topicWiseResults.size() == 0) {
			out.println("<font face=verdana size=2 color=red align=justify><div align=justify>Sorry, no results found. We support only Java methods and 156 topics in this prototype. This prototype uses stackoverflow dataset only. Hence, the structure of this input code should match with a code snippet in stackoverflow.</div></font>");
		}
} catch (Exception e) {e.printStackTrace(); out.println(e.getMessage());}
 
	}
%>
    			</td>
    		</tr>
    		<tr>
    			<td colspan=2 width=600px><hr color=brown><font size=2px color=gray>This is a research product from the program analysis group at IIIT Delhi. We are grateful to Microsoft Research and Confederation of Indian Industries (CII) for their support.</font></td>
    			</tr>
   
    	</table>
    </td>
  </tr>
</table>