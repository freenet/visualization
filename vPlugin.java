/*set up package & imports*/

import freenet.node.LocationManager;
import freenet.node.Node;
import freenet.node.PeerNode;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.PluginRespirator;
import java.util.Date;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;


public class vPlugin extends JFrame implements FredPlugin { // Set up the visualization frame so that it extends from a JFrame; every Freenet plugin needs FredPlugin class to get the PluginRespirator

    /*some vars*/
    private int xC;
    private int yC;
    private volatile boolean goon = true;

    PluginRespirator pr;

    private BufferedImage buf;
    private int oldS;       // Variable to check if frame size has changed
    private double myLoc;   // Variable to save the Location information

    public void terminate() {
        this.goon = false;  // The method called in order to break the loop and terminate the plugin
    }
public static void main(String args[]){
	System.out.println("Plugin started");
}
    public void runPlugin(PluginRespirator pr) {
      
        this.pr = pr; // Maps the plugin interface to the own class
    
        
        this.setAlwaysOnTop(true); // This member keeps the image always above other programs. Options are: "true" or "false."
        this.setSize(new Dimension(320, 240)); // This member defines the dimensions of the initial window.
        setVisible(true); // This member defines the visibility of the window.



        /* This statement is automatically called each time a new packet arrives. */
        while (goon) {
            System.err.println("---->: " + (new Date()));   // Member to mark a control timer in the console
     
            Node node = pr.getNode();                       // Variable to get the node information from Freenet over the plugin respirator
            LocationManager ln = node.getLocationManager(); // Variable to get a location object (see Freenet documentation)
            

            if (ln != null) { // Statement to check if there is information

                String s = ln.toString();                   // Member to transform to readable information
                s += "_loc:_" + ln.getLocation();           // Creates information string for output when it is in use
                this.myLoc = ln.getLocation();              // Gets the location of the node
                PeerNode[] p = node.getConnectedPeers();    // Gets the connected nodes
                


                if (p != null) {             // Statement to check if there is a connection
                    if (p.length > 0) {      // Statement to check if there is information in that connection

                        drawBack(); // Member to draw/overpaint with a little transparency


                        int offset = 3; // Location, inBytes, outBytes

                        for (int i = 0; i < p.length; i++) { // This statement is automatically called until there is a connection between nodes
                            drawPoint(buf.getGraphics(), p[i]); // Member for drawing the points and information
                        }
                        paint(this.getGraphics()); // Member for displaying graphics
                    }
                }
   
            } else { // Informs over console about no information
                System.out.println("no_informadrawtion");
            }

            try {
                Thread.sleep(1000); // Set the scan of new connections to 1 second
            } catch (InterruptedException e) {
                // Terminate
            }
        }
    }

    @Override
    public void paint(Graphics g) { // Method for handler update while drawing graph
        if (g == null) {
            return;
        }
         g.drawImage(this.buf, 0, 0, this);
         }

    @Override
    public void update(Graphics g) {
    }

    private void drawBack() {
        if (this.getGraphics() == null) { // Method to check if there is a graphic context
            return;
        }

        if (this.buf == null || oldS != getSize().width) { // grafic context updater_react on dragging and refreshing the visualisator
            oldS = getSize().width;
            System.out.println("fresh");
            this.buf = new BufferedImage(getSize().width, getSize().height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = this.buf.createGraphics();
            this.print(graphics);
            graphics.dispose();

            this.xC = (buf.getWidth() / 2);
            this.yC = (buf.getHeight() / 2);

        } // end update

        Color clearColor = new Color(220, 0, 0); // Slowly fade out of the graph
        Graphics2D g2 = (Graphics2D) buf.getGraphics();
        g2.setColor(new Color(0, 0, 0, 182));
        g2.fillRect(0, 0, this.buf.getWidth(), this.buf.getHeight()); // Overpaint graph

        float colW;
      
        int laenge = (int) (this.yC / 1.6);
        int x2 = 0;
        int y2 = 0;
        g2.setColor(Color.gray);
        g2.drawOval((this.buf.getWidth() / 2) - (laenge), (this.buf.getHeight() / 2) - (laenge),
                laenge * 2, laenge * 2);
    }


    /* Method to draw nodes based on the information and activity */
    private void drawPoint(Graphics graphics, PeerNode peerNode) {


        /* Mapping information to a circle */
        int r = (int) (this.yC / 1.6);
        double actT = peerNode.getLocation() * 360f;


        int x2 = this.xC - (int) (Math.sin(Math.toRadians(actT)) * r);
        int y2 = this.yC - (int) (Math.cos(Math.toRadians(actT)) * r);


        int mLocx = this.xC - (int) (Math.sin(Math.toRadians(myLoc * 360d)) * r);
        int mLocy = this.yC - (int) (Math.cos(Math.toRadians(myLoc * 360d)) * r);

        Color colMyLoc = Color.getHSBColor((float) (myLoc / 360f), .5f, 0.5f);
        Color col = Color.getHSBColor((float) (actT / 360f), 0.5f, 0.5f);
       
        graphics.setColor(col);


        int TotalInputBytesR = (int) ((peerNode.getTotalInputBytes() / 1000) * 2);
        int TotalOutputByteR = (int) ((peerNode.getTotalOutputBytes() / 1000) * 2);


        // Reduces traffic of the node
        TotalInputBytesR%=60;
        TotalOutputByteR%=60;


        graphics.fillOval(x2 - TotalInputBytesR/2, y2 - TotalInputBytesR/2, TotalInputBytesR, TotalInputBytesR);

        col = Color.getHSBColor((float) (actT / 360f), 1f, 0.5f);
        
        graphics.setColor(col);
        graphics.drawOval(x2 - TotalOutputByteR/2, y2 - TotalOutputByteR/2, TotalOutputByteR, TotalOutputByteR);

        col = Color.getHSBColor((float) (actT / 360f), 0.5f, 0.5f);
        line(mLocx, mLocy, x2, y2, col);

        graphics.setColor(colMyLoc);
        graphics.fillRect(mLocx - 3, mLocy - 3, 8, 8); // Draws a position of the node
        
    }   


    /* Method to draw connection of the nodes based on the information and activity */
    public void line(int x, int y, int x2, int y2, Color color) {
   
        int w = x2 - x;
        int h = y2 - y;
        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
        if (w < 0) {
            dx1 = -1;
        } else if (w > 0) {
            dx1 = 1;
        }
        if (h < 0) {
            dy1 = -1;
        } else if (h > 0) {
            dy1 = 1;
        }
        if (w < 0) {
            dx2 = -1;
        } else if (w > 0) {
            dx2 = 1;
        }
        int longest = Math.abs(w);
        int shortest = Math.abs(h);
        if (!(longest > shortest)) {
            longest = Math.abs(h);
            shortest = Math.abs(w);
            if (h < 0) {
                dy2 = -1;
            } else if (h > 0) {
                dy2 = 1;
            }
            dx2 = 0;
        }
        int numerator = longest >> 1;
        for (int i = 0; i <= longest; i++) {
              buf.setRGB(x, y, color.getRGB());


            numerator += shortest;
            if (!(numerator < longest)) {
                numerator -= longest;
                x += dx1;
                y += dy1;
            } else {
                x += dx2;
                y += dy2;
            }
        }
    }
}