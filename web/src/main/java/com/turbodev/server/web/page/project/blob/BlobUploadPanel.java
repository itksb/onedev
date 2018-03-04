package com.turbodev.server.web.page.project.blob;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;

import com.turbodev.server.event.RefUpdated;
import com.turbodev.server.model.Project;
import com.turbodev.server.web.component.dropzonefield.DropzoneField;
import com.turbodev.server.web.page.project.blob.render.BlobRenderContext;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
abstract class BlobUploadPanel extends Panel {

	private final BlobRenderContext context;
	
	private String directory;
	
	private String summaryCommitMessage;
	
	private String detailCommitMessage;
	
	private final Collection<FileUpload> uploads = new ArrayList<>();
	
	public BlobUploadPanel(String id, BlobRenderContext context) {
		super(id);
		this.context = context;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		form.setMultiPart(true);
		form.setFileMaxSize(Bytes.megabytes(Project.MAX_UPLOAD_SIZE));
		add(form);
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		NotificationPanel feedback = new NotificationPanel("feedback", form);
		feedback.setOutputMarkupPlaceholderTag(true);
		form.add(feedback);
		
		form.add(new DropzoneField("files", 
				new PropertyModel<Collection<FileUpload>>(this, "uploads"), null, 0, Project.MAX_UPLOAD_SIZE).setRequired(true));
		form.add(new AjaxButton("upload") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				String commitMessage = summaryCommitMessage;
				if (StringUtils.isBlank(commitMessage))
					commitMessage = "Add files via upload";
				
				if (StringUtils.isNotBlank(detailCommitMessage))
					commitMessage += "\n\n" + detailCommitMessage;

				RefUpdated refUpdated = context.uploadFiles(uploads, directory, commitMessage);
				onCommitted(target, refUpdated);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(feedback);
			}
			
		});
		
		form.add(new TextField<String>("directory", new PropertyModel<String>(this, "directory")));
		form.add(new TextField<String>("summaryCommitMessage", 
				new PropertyModel<String>(this, "summaryCommitMessage")));
		form.add(new TextArea<String>("detailCommitMessage", new PropertyModel<String>(this, "detailCommitMessage")));
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
	}

	abstract void onCommitted(AjaxRequestTarget target, RefUpdated refUpdated);
	
	abstract void onCancel(AjaxRequestTarget target);
	
}
