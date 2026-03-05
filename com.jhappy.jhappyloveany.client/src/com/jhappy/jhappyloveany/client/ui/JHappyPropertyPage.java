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
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
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
	 * テーブル画面の生成
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
		Image reloadIcon = PlatformUI.getWorkbench().getSharedImages()
				.getImage(org.eclipse.ui.ISharedImages.IMG_ELCL_SYNCED);
		reloadBtn.setImage(reloadIcon);
		reloadBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		reloadBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateUIVisibility();
			}
		});

		viewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		ColumnViewerToolTipSupport.enableFor(viewer);

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		// ★ 共通化したcreateColumnを使用（第5引数にバリデーションキーを指定）
		createColumn(viewer, Messages.PropertyPage_Column_Type, 80,
				m -> m.getType(), "type");

		createColumn(viewer, Messages.PropertyPage_Column_FilePattern, 180,
				m -> m.getFilepath(), "path");

		createColumn(viewer, Messages.PropertyPage_Column_XPath, 200,
				m -> "properties".equals(m.getType()) ? "-" : m.getXpath(), "xpath");

		createColumn(viewer, Messages.PropertyPage_Column_Description, 200,
				m -> m.getDescription(), null); // 説明欄はバリデーションなし

		viewer.setContentProvider(ArrayContentProvider.getInstance());
	}

	/**
	 * カラム生成の共通メソッド
	 * propertyKeyを指定すると、QueryModel内のバリデーション結果と自動連動します
	 */
	private void createColumn(TableViewer viewer, String title, int width,
			java.util.function.Function<QueryModel, String> textProvider,
			String propertyKey) {

		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText(title);
		col.getColumn().setWidth(width);

		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return textProvider.apply((QueryModel) element);
			}

			@Override
			public Image getImage(Object element) {
				if (propertyKey == null)
					return null;
				QueryModel m = (QueryModel) element;
				ValidationResult result = m.getValidation(propertyKey);
				if (!result.isValid) {
					return PlatformUI.getWorkbench().getSharedImages()
							.getImage(org.eclipse.ui.ISharedImages.IMG_OBJS_ERROR_TSK);
				}
				return null;
			}

			@Override
			public String getToolTipText(Object element) {
				if (propertyKey == null)
					return null;
				QueryModel m = (QueryModel) element;
				return m.getValidation(propertyKey).message;
			}
		});
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

				QueryModel m = new QueryModel(
						el.getAttribute("filepath"),
						el.getAttribute("type"),
						el.getAttribute("trim"),
						el.getTextContent().trim(),
						el.getAttribute("description"));

				// ★ バリデーションの実行（ロジックを外出ししたValidatorを適用）
				m.runValidation("type",
						QueryValidators.compose(QueryValidators.TYPE_REQUIRED, QueryValidators.TYPE_KIND));
				m.runValidation("path", QueryValidators.compose(
						QueryValidators.FILEPATH_REQUIRED, QueryValidators.FILEPATH_REGEX));
				m.runValidation("xpath", QueryValidators.XPATH_SPECIFIC);

				models.add(m);
			}
			viewer.setInput(models);

			boolean hasAnyError = models.stream().anyMatch(QueryModel::hasError);
			setErrorMessage(hasAnyError ? "設定ファイルに不正な記述があります。アイコンを確認してください。" : null);

		} catch (Exception e) {
			setErrorMessage("XMLパースエラー: " + e.getMessage());
		}
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

		Label label = new Label(labelComposite, SWT.WRAP);
		label.setText(Messages.PropertyPage_Label_FileNotFound);
		label.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));

		GridData labelData = new GridData(SWT.FILL, SWT.TOP, true, false);

		labelData.widthHint = 400;
		label.setLayoutData(labelData);

		label.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));

		createHelpLink(emptyComposite);

		Button createBtn = new Button(emptyComposite, SWT.PUSH);
		createBtn.setText(Messages.PropertyPage_Button_CreateDefaultConfig);
		createBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		Image newFileIcon = PlatformUI.getWorkbench().getSharedImages()
				.getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_ADD);
		createBtn.setImage(newFileIcon);
		createBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createDefaultConfigFile();
				updateUIVisibility();
			}
		});
	}

	/**
	 *
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

		mainComposite.layout(true, true);
	}

	/**
	 * プロジェクト内の雛形ファイルからデフォルトのXMLファイルを生成
	 */
	private void createDefaultConfigFile() {
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		IFile configFile = project.getFile("jhappyqueries.xml");

		try (InputStream templateStream = getTemplateFromBundle()) {

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

	//
	private InputStream getTemplateFromBundle() throws Exception {
		org.osgi.framework.Bundle bundle = org.eclipse.core.runtime.Platform
				.getBundle("com.jhappy.jhappyloveany.client");
		java.net.URL url = bundle.getEntry("/resources/jhappyqueries.xml");
		return url.openStream();
	}

}