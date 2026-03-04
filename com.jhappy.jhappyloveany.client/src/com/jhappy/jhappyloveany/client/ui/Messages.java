package com.jhappy.jhappyloveany.client.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.jhappy.jhappyloveany.client.ui.messages"; //$NON-NLS-1$
    
    public static String PropertyPage_Label_FileNotFound;
    public static String PropertyPage_Link_Help;
    public static String PropertyPage_Button_CreateDefault;
    public static String PropertyPage_Column_Type;
    public static String PropertyPage_Column_FilePattern;
    public static String PropertyPage_Column_XPath;
    public static String PropertyPage_Column_Description;
    public static String PropertyPage_Msg_SuccessTitle;
    public static String PropertyPage_Msg_SuccessBody;
    public static String PropertyPage_Msg_ErrorTitle;
    public static String PropertyPage_Msg_ErrorBody;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {}
}