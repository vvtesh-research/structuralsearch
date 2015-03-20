
	
	<html>
    <head><title>jSense</title>
    	<script>
    	function resetForm()
		{
			code1.value = "";
			code2.value = "";
		}
    	</script>
    </head>
    	
    <body>
    	
    <form method=post action=result.jsp>
    <table width=100% height=98%>
    	<tr>
    		<td align=center valign=top>
    <table width=800px cellpadding=0 cellspacing=0>
    	<tr>
    	<td colspan=2><img src=jsenselogo.png height=100px/>
    				<hr color=brown>
    	</td>
    	</tr>
    <tr>
    	<td width=400px align=justify><font face=verdana size=2px>Are you confused about what a tiny bit of code in front of you, is doing actually? Do you suspect that, this code can perhaps be improved with crowd knowledge? Do you have no idea how to locate this code in stackoverflow discussions? jSense can help you. 
    	<br><br>Paste your input program here. jSense scans each Java method in this box, identifies the topic associated with it, searches for structurally similar methods in stackoverflow, and returns variant implementations of the topic implemented in the methods.
    	<br><br>We plan to release eclipse plugins so that developers can get live feedback as they code. In this prototype, jSense is capable of recognizing these <a href=156Topics.txt>156 topics</a>. jSense works with a repository of 51547 Java methods mined from stackoverflow. We show that structural information in source code is useful in program comprehension and can be effectively indexed and queried.
    	<br><br>Your suggestions for improvement are welcome. Please contact venkateshv@iiitd.ac.in.
    	</font></td>
    	
    	<td width=200px align=center>Paste your input program here:<br>
    	<textarea rows="20" cols="50" id=code1 name=code1>
class MyCode {
   public static BigInteger factorialIterative(int n)
   {
        if(n == 0 || n == 1) return BigInteger.valueOf(1);
        BigInteger f = BigInteger.valueOf(1);
        for(int i = 1 ; i &lt;= n ;i++)
        f = f.multiply(BigInteger.valueOf(i));
        return f;
   }
}
			</textarea><br><br>&nbsp;<input type=submit value="Submit" target="result.jsp"></input>&nbsp;&nbsp;<input type=button value=Clear onClick="resetForm();"></input><br>&nbsp;
		</td>
   	</tr>
    <tr bgcolor=brown>
    <td align=center colspan=2></td>
    </tr>
    			<tr>
    			<td colspan=2 width=600px><hr color=brown><font size=2px color=gray>This is a research product from the program analysis group at IIIT Delhi. We are grateful to Microsoft Research and Confederation of Indian Industries (CII) for their support.</font></td>
    			</tr>
    </table>
    			</td>
    			</tr>
    			</table>
    
    </form>
	    </body>
    
</html>