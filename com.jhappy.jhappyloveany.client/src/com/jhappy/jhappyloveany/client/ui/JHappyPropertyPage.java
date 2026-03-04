package com.jhappy.jhappyloveany.client.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.ide.IDE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class JHappyPropertyPage extends PropertyPage {
	private TableViewer viewer;
	private Composite mainComposite;
	private Composite tableComposite;
	private Composite emptyComposite;
	private StackLayout stackLayout;

	private static final String HELP_ID = "com.jhappy.jhappyloveany.help_context_id";

	@Override
	protected Control createContents(Composite parent) {
		
		noDefaultAndApplyButton();
		
		mainComposite = new Composite(parent, SWT.NONE);
		stackLayout = new StackLayout();
		mainComposite.setLayout(stackLayout);
		
		noDefaultAndApplyButton();

		createTableArea(mainComposite);
		createEmptyArea(mainComposite);

		updateUIVisibility();

		return mainComposite;
	}

	/**
     * テーブル画面：上部にヘルプと再読み込みボタン、中央にテーブルを配置
     */
    private void createTableArea(Composite parent) {
        tableComposite = new Composite(parent, SWT.NONE);
        tableComposite.setLayout(new GridLayout(1, false));

        // 操作用バー
        Composite actionBar = new Composite(tableComposite, SWT.NONE);
        actionBar.setLayout(new GridLayout(2, false));
        actionBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        createHelpLink(actionBar);

        Button reloadBtn = new Button(actionBar, SWT.PUSH);
        reloadBtn.setText(Messages.Reload_Config);
        reloadBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        reloadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateUIVisibility();
            }
        });

        viewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        createColumn(viewer, Messages.PropertyPage_Column_Type, 80);
        createColumn(viewer, Messages.PropertyPage_Column_FilePattern, 180);
        createColumn(viewer, Messages.PropertyPage_Column_XPath, 200);
        createColumn(viewer, Messages.PropertyPage_Column_Description, 200);

        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setLabelProvider(new QueryLabelProvider());
    }

	/**
	 * ファイルなし画面：中央寄せでメッセージ、リンク、ボタンを配置
	 */
	private void createEmptyArea(Composite parent) {

		emptyComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 30;
		layout.marginWidth = 30;
		layout.verticalSpacing = 20;
		emptyComposite.setLayout(layout);

		Composite labelComposite = new Composite(emptyComposite, SWT.NONE);
		labelComposite.setLayout(new GridLayout(2, false));

		Label iconLabel = new Label(labelComposite, SWT.NONE);
		iconLabel.setImage(parent.getDisplay().getSystemImage(SWT.ICON_WARNING));
		// 1. SWT.WRAP を指定して改行を許可する
		Label label = new Label(labelComposite, SWT.WRAP);
		label.setText(Messages.PropertyPage_Label_FileNotFound);
		label.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));

		// 2. GridDataの設定
		GridData labelData = new GridData(SWT.FILL, SWT.TOP, true, false);
		// widthHint を設定すると、その幅を超えた時に改行されます
		// 400ピクセル程度を基準にするのが一般的です
		labelData.widthHint = 400;
		label.setLayoutData(labelData);

		label.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));

		createHelpLink(emptyComposite);

		Button createBtn = new Button(emptyComposite, SWT.PUSH);
		createBtn.setText("デフォルトの設定ファイルを作成する");
		createBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		createBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createDefaultConfigFile();
				updateUIVisibility();
			}
		});
	}

	/**
	 * リンクが表示されない問題を解決するために、メソッド化して確実にレイアウトを適用
	 */
	private void createHelpLink(Composite parent) {
		Link link = new Link(parent, SWT.NONE);
		link.setText(Messages.PropertyPage_Link_Help);
		link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(HELP_ID);
			}
		});
	}

	private void updateUIVisibility() {
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		IFile configFile = project.getFile("jhappyqueries.xml");

		if (configFile != null && configFile.exists()) {
			stackLayout.topControl = tableComposite;
			loadConfigData();
		} else {
			stackLayout.topControl = emptyComposite;
		}

		// 子階層まで再配置を強制
		mainComposite.layout(true, true);
	}

	/**
     * プロジェクト内の雛形ファイルからデフォルトのXMLファイルを生成
     */
    private void createDefaultConfigFile() {
        IProject project = (IProject) getElement().getAdapter(IProject.class);
        IFile configFile = project.getFile("jhappyqueries.xml");
        
   

        try (InputStream   templateStream = getTemplateFromBundle()){
           
            // 新しいファイルを作成（templateStream は内部で close される）
            configFile.create(templateStream, true, null);

            MessageDialog.openInformation(getShell(), 
                Messages.PropertyPage_Msg_SuccessTitle, 
                Messages.PropertyPage_Msg_SuccessBody);
            
         // 作成したファイルを Eclipse エディタで開く
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IDE.openEditor(page, configFile);

        } catch (Exception e) {
            MessageDialog.openError(getShell(), 
                Messages.PropertyPage_Msg_ErrorTitle, 
                NLS.bind(Messages.PropertyPage_Msg_ErrorBody, e.getMessage()));
        }
    }

	// プラグイン内の "/templates/default_config.xml" を読み込む場合
	private InputStream getTemplateFromBundle() throws Exception {
		org.osgi.framework.Bundle bundle = org.eclipse.core.runtime.Platform
				.getBundle("com.jhappy.jhappyloveany.client");
		java.net.URL url = bundle.getEntry("/resources/jhappyqueries.xml");
		return url.openStream();
	}

	private void createColumn(TableViewer viewer, String title, int width) {
		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText(title);
		col.getColumn().setWidth(width);
	}

	private void loadConfigData() {
		List<QueryModel> models = new ArrayList<>();
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		IFile configFile = project.getFile("jhappyqueries.xml");

		if (!configFile.exists())
			return;

		try (InputStream is = configFile.getContents()) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);

			NodeList nodeList = doc.getElementsByTagName("query");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element el = (Element) nodeList.item(i);

				String xpath = el.getTextContent();
				if (xpath != null) {
					xpath = xpath.trim();
				}

				models.add(new QueryModel(
						el.getAttribute("filepath"),
						el.getAttribute("type"),
						el.getAttribute("trim"),
						xpath,
						el.getAttribute("description")));
			}
			viewer.setInput(models);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}