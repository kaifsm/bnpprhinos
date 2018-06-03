import javax.swing.ImageIcon;

public class Main
{
	public static void main(String[] args)
	{
		MyFrame f = new MyFrame();
		ImageIcon img = new ImageIcon("resources/rhino.png");
		f.setIconImage(img.getImage());
		f.setVisible(true);
	}
}
