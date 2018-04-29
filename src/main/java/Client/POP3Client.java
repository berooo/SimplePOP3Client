package Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class POP3Client {
	
	private static Socket socket=null;
	private static boolean debug=true;
	
	public POP3Client(String server,int port) {
		
		try {
			socket=new Socket(server,port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
		
		}
		
	}
	//user命令
	public static void user(String user,BufferedReader in,BufferedWriter out) throws IOException {
		String result=null;
		//先检测连接服务器是否成功
		result=getResult(getReturn(in));
		
		if(!"+OK".equals(result)) {
			throw new IOException("Connecting is fail");
		}
		//发送user命令
		result=getResult(sendServer("user "+user,in,out));
		
		if(!"+OK".equals(result)) {
			throw new  IOException("Username is wrong");
		}
	}
	//pass命令
	public static void pass(String password,BufferedReader in,BufferedWriter out) throws IOException {
		
		String result=null;
		result=getResult(sendServer("pass "+password,in,out));
		if(!"+OK".equals(result)) {
			throw new  IOException("Password is wrong");
		}
		
	}
	//stat命令
	public int stat(BufferedReader in,BufferedWriter out) throws IOException {
		
		String result=null;
		String line=null;
		int mailNum=0;
		
		line=sendServer("stat",in,out);
		
		String[] t=line.split(" ");
		
		
		if(t.length>1) {
			mailNum=Integer.parseInt(t[1]);
		}
		result=t[0];
		if(!"+OK".equals(result)) {
			System.out.println("There are errors when checking mail status.");
		}
		
		System.out.println("The number of mails is"+mailNum);
		return mailNum;
	}
	
	//无参数list命令
	public void list(BufferedReader in,BufferedWriter out) throws IOException {
		String message="";
		String line=null;
		line=sendServer("list",in,out);
		while(!".".equalsIgnoreCase(line)) {
			message=message+line+"\n";
			line=in.readLine().toString();
		}
		System.out.println(message);
	}
	//带参数list命令
	public void list_one(int mailNum,BufferedReader in,BufferedWriter out) throws IOException {
		
		String result=null;
		result=getResult(sendServer("list "+mailNum,in,out));
		if(!"+OK".equals(result)) {
			throw new IOException("list错误");
		}
	}
	//得到邮件详细信息
	public String getMessagedetail(BufferedReader in) throws IOException {
		
		String message="";
		String line=null;
		line=in.readLine().toString();
		while(!".".equalsIgnoreCase(line)) {
			message=message+line+"\n";
			line=in.readLine().toString();
		}
		
		return message;
	}
	
	//retr命令
	public void retr(int mailNum,BufferedReader in,BufferedWriter out) throws IOException, InterruptedException {
		
		String result=null;
		result=getResult(sendServer("retr "+mailNum,in,out));
		
		if(!"+OK".equals(result)) {
			throw new IOException("接收邮件出错！");
		}
		
		System.out.println("No"+mailNum);
		System.out.println(getMessagedetail(in));
		Thread.sleep(3000);
	}
	private static String sendServer(String str,BufferedReader in,BufferedWriter out) throws IOException {
		
		//发送命令
		out.write(str);
		//发送空行
		out.newLine();
		//清空缓冲区
		out.flush();
		
		return getReturn(in);
		
	}
	//从返回的命令里面得到第一个字段，也就是服务器的返回状态码(+OK或者-ERR)
	public static String getResult(String line) {
		
		String[] t=line.split(" ");
		
		return t[0];
		
	}
	public static String getReturn(BufferedReader in) {
		
		
		String line="";
		try {
		
			line=in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return line;
		
	}
	public static void quit(BufferedReader in,BufferedWriter out) throws Exception {
		
		String result;
		result=getResult(sendServer("QUIT",in,out));
		if(!"+OK".equals(result)) {
			throw new IOException("exit error");
		}
	}
	public boolean receiveMail(BufferedReader in,BufferedWriter out,String command) throws Exception {
		
		try {
			
			if(command.equalsIgnoreCase("stat")) {
				stat(in,out);
			}else if(command.equalsIgnoreCase("list")) {
				list(in,out);
			}else if(command.substring(0, 4).equalsIgnoreCase("retr")) {
				String[] t=command.split(" ");
				int n=Integer.parseInt(t[1]);
				retr(n,in,out);
			}else {
				
				System.out.println("Please input a right command");
			}
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public  static void main(String[] args) throws Exception {
		String server="pop3.163.com";
	//	String user="diamond_br@163.com";
	//	String password="b13992128496";
		
		Scanner input=new Scanner(System.in);
		System.out.println("please input username:");
		String user=input.next();
		System.out.println("please input userpassword：");
		String password=input.next();
		
		POP3Client pop3client=new POP3Client(server,110);
		BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		BufferedWriter out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		
		user(user,in,out);
		pass(password,in,out);
		
		System.out.println("connected");
		
		String command=input.next();
		
		while(!command.equalsIgnoreCase("quit")) {
			pop3client.receiveMail(in,out,command);
			command=input.next();
			if(command.equalsIgnoreCase("retr")) {
				command+=" "+input.next();
			}
		}
		
		quit(in,out);
		System.out.println("bye");
		input.close();
		
	}
}
