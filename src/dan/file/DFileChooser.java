package dan.file;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Dan
 */
public class DFileChooser extends JFileChooser implements PropertyChangeListener{

    /**
     * FilterType.
     */
    public static final int ONLY_IMAGES = 0;

    /**
     * Set file filtering by typed text.
     */
    public static final int SELECTIVE = 4;

    /**
     * Set file filtering by typed text and autocomplete it.
     */
    public static final int SELECTIVE_AUTO_COMPLETE = 5;

    JTextField tf; // Pointer to the textfield in the JFileChooser.
    DocumentListener dl;
    FileFilter originalFF;
    boolean ignoreModify = false, autoComplete = false;

    public DFileChooser() {
        super();
        initialize();
    }

    public DFileChooser(File f) {
        super(f);
        initialize();
    }

    public DFileChooser(String s) {
        super(s);
        initialize();
    }

    private void initialize() {
        tf = (JTextField) ((JPanel) ((JPanel) this.getComponent(3)).getComponent(0)).getComponent(1);
        addPropertyChangeListener(this);
    }

    /**
     * Used to set file filtering based on typed text
     * and/or text automatic completion.
     * @param mode
     * <ul>
     * <li> DFileChooser.SELECTIVE
     * <li> DFilechooser.SELECTIVE_AUTO_COMPLETE
     * </ul>
     */
    public void setAutoCompleteMode(int mode) {
        if (mode == SELECTIVE_AUTO_COMPLETE)
            autoComplete = true;
        if (mode>=SELECTIVE) {
            dl = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    if (e.getLength()<2 && !ignoreModify)
                        modifyFilter(0);
                }
                public void removeUpdate(DocumentEvent e) {
                    if (e.getLength()<2 && !ignoreModify)
                        modifyFilter(1);
                }
                public void changedUpdate(DocumentEvent e) {
                    if (e.getLength()<2 && !ignoreModify)
                        modifyFilter(2);
                }
            };
            tf.getDocument().addDocumentListener(dl);
        }
        else if (tf != null)
            tf.getDocument().removeDocumentListener(dl);
    }

    private synchronized void modifyFilter(int n) {
        String text = tf.getText();
        if (originalFF == null)
            originalFF = this.getFileFilter();
        if (text.isEmpty()) {
            this.setFileFilter(originalFF);
            return;
        }

        AutoCompleteFilter acf = new AutoCompleteFilter(text, n==1);
        this.setFileFilter(acf);
    }

    public void addChoosableFileFilter(int filterType) {
        switch (filterType) {
            case ONLY_IMAGES:
                this.addChoosableFileFilter(new ImageFilter());
                break;
        }
    }

    public void addChoosableFileFilter(String ... extensions) {
        this.addChoosableFileFilter(new CustomFileFilter(extensions));
    }

    public void setFileFilter(int filterType) {
        switch (filterType) {
            case ONLY_IMAGES:
                this.setFileFilter(new ImageFilter());
                break;
        }
    }

    public void setFileFilter(String ... extensions) {
        this.setFileFilter(new CustomFileFilter(extensions));
    }

    public void addImageThumbnail() {
        this.setAccessory(new ImagePreview(this));
    }


    @Override
    public void approveSelection() {
        File f = getSelectedFile();
        if(f.exists() && getDialogType() == SAVE_DIALOG) {
            int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
            switch(result){
                case JOptionPane.YES_OPTION:
                    super.approveSelection();
                    return;
                case JOptionPane.NO_OPTION:
                    return;
                case JOptionPane.CLOSED_OPTION:
                    return;
                case JOptionPane.CANCEL_OPTION:
                    cancelSelection();
                    return;
            }
        }
        super.approveSelection();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
//        String prop = evt.getPropertyName();
//
//        if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
//            tf.requestFocus();
//            tf.select(0, tf.getText().length());
//        }
    }


    /**
     * Implements a FileFilter for specified extensions
     * on constructor method.
     */
    private class CustomFileFilter extends FileFilter {

        String[] extensions;

        public CustomFileFilter(String ... extensions) {
            super();
            this.extensions = extensions;
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;

            String extension = getExtension(f);
            if (extension != null) {
                for (int n=0;n<extensions.length;n++) {
                    if (extension.equals(extensions[n]))
                        return true;
                }
            }

            return false;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    /**
     * CustomFileFilter constructor for images.
     */
    private class ImageFilter extends CustomFileFilter {

        public ImageFilter() {
            super("jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif");
        }

        @Override
        public String getDescription() {
            return "Image Files(jpg, jpeg, gif, tif, tiff, png, bmp)";
        }
    }

    private class AutoCompleteFilter extends FileFilter {
        String text = "";
        boolean fileFound = false;

        public AutoCompleteFilter(String t, boolean f) {
            super();
            text = t;
            fileFound = f;
        }

        @Override
        public boolean accept(File f) {
            boolean accept = (f.isDirectory() || f.getName().startsWith(text));
            if (accept && !f.getName().equals(tf.getText()) && !f.isDirectory() && !fileFound && autoComplete) {
                fileFound = true;
                ignoreModify = true;
                tf.setText(f.getName());
                tf.select(text.length(), tf.getText().length());
                ignoreModify = false;
            }
            return accept;
        }

        @Override
        public String getDescription() {
            return null;
        }

    }

    public String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
