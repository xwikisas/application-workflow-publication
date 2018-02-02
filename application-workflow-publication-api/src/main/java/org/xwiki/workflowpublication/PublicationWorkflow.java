/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.workflowpublication;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * @version $Id$
 */
@Role
public interface PublicationWorkflow
{
    /**
     * The reference to the workflow class, relative to the current wiki.
     */
    public static final EntityReference PUBLICATION_WORKFLOW_CLASS = new EntityReference("PublicationWorkflowClass",
        EntityType.DOCUMENT, new EntityReference("PublicationWorkflow", EntityType.SPACE));

    boolean isWorkflowDocument(XWikiDocument document, XWikiContext context) throws XWikiException;

    /**
     * Returns {@code true} if the two documents are different, {@code false} otherwise. Note that this function ignores
     * changes in comments, rights and publication workflow objects, as well as metadata changes such as dates and
     * authors. It is made to compare a published document with a draft document to decide whether one needs to be
     * overwritten with the other (on unpublish).
     * 
     * @param fromDoc the source document
     * @param toDoc the destination document
     * @param context the XWiki context
     * @return {@code true} if the two documents are different, {@code false} otherwise
     * @throws XWikiException in case of problems
     */
    boolean isModified(XWikiDocument fromDoc, XWikiDocument toDoc, XWikiContext context) throws XWikiException;

    /**
     * @param doc the document reference
     * @param workflowConfig the workflow configuration
     * @param target the destination document
     * @param xcontext the XWiki context
     * @return {@code true} if the workflow starts, {@code false} otherwise
     * @throws XWikiException in case of problems
     */
    public boolean startWorkflow(DocumentReference doc, String workflowConfig, DocumentReference target,
        XWikiContext xcontext) throws XWikiException;

    /**
     * Starts the workflow on {@code target} as the published document, without creating the draft document. The draft
     * can be created the first time when the function {@link #createDraftDocument(DocumentReference, XWikiContext)}
     * will be called on this published document. Roughly this function is only setting up the marker on {@code target}
     * as a published documemt. It does, however, all verifications (that there is no other worflow on that document,
     * etc).
     * 
     * @param target the target document reference
     * @param workflowConfig the configuration
     * @param xcontext the XWiki context
     * @return {@code true} if the workflow starts on {@code target} as the published document, {@code false} otherwise
     * @throws XWikiException in case of problems
     */
    public boolean startWorkflowAsTarget(DocumentReference target, String workflowConfig, XWikiContext xcontext)
        throws XWikiException;

    /**
     * Gets the draft document corresponding to the passed target: meaning the workflow document which is not a target
     * and which has the passed target as target. Note that this function looks for the draft document on the same wiki
     * as the passed target document.
     * 
     * @param targetRef the target document reference
     * @param xcontext the XWiki context
     * @return the draft document reference
     * @throws XWikiException in case of problems
     */
    public DocumentReference getDraftDocument(DocumentReference targetRef, XWikiContext xcontext) throws XWikiException;

    /**
     * Gets the draft document corresponding to the passed target: meaning the workflow document which is not a target
     * and which has the passed target as target. Note that this function looks for the document in the passed wiki. The
     * target is compared to the target relative to the passed wiki: without wiki reference if the target is in the same
     * wiki as the passed wiki and fully prefixed if they're in different wikis. This function should be called when,
     * for a reason or another, the draft is not in the same wiki as the target document.
     * 
     * @param wiki parameter should be the wiki where the draft is expected to be.
     * @param targetRef the target document reference
     * @param wiki the wiki name
     * @param xcontext the XWiki context
     * @return the draft document reference
     * @throws XWikiException in case of problems
     * @since 1.1
     */
    public DocumentReference getDraftDocument(DocumentReference targetRef, String wiki, XWikiContext xcontext)
        throws XWikiException;

    /**
     * Creates a draft document corresponding to the passed target reference, which will have as a target the passed
     * reference. The draft document is created in the same wiki, the space where the document is created is taken from
     * the defaultDraftsSpace property of the workflow config of the target and the name of the draft document is an
     * unique name generated starting from the target document.
     * 
     * @param targetRef the target document reference
     * @param xcontext the XWiki context
     * @return the draft document reference
     * @throws XWikiException in case of problems
     */
    public DocumentReference createDraftDocument(DocumentReference targetRef, XWikiContext xcontext)
        throws XWikiException;

    /**
     * Sets up the draft rigths and visibility on the passed XWikiDocument, as a draft document. It's a helper function
     * for the listeners to be able to setup the rights with one function. Workflow groups configuration will be taken
     * from the workflow object that is attached to the passed document. If such workflow config does not exist, the
     * document will not be changed. Note that this function does not save the document, it just changes it.
     * 
     * @param document the document to set the draft access settings on
     * @param xcontext the context of the modification
     * @throws XWikiException in case something goes wrong
     */
    public void setupDraftAccess(XWikiDocument document, XWikiContext xcontext) throws XWikiException;

    /* Functions to be used from the scripts */

    /**
     * draft -> contributing + moderator gets explicit rights for edit and contributor does not have anymore. <br>
     * If there are no defined moderators, this will delegate to submitForValidation.
     * 
     * @param document is the draft document which needs to pass in moderating state
     * @return {@code true} if the document is submitted for moderation, {@code false} otherwise
     * @throws XWikiException
     */
    public boolean submitForModeration(DocumentReference document) throws XWikiException;

    /**
     * moderating -> draft + contributor gets edit rights back
     * 
     * @param document the document reference to be moderated
     * @param reason the reason for refusing moderation
     * @return {@code true} if the moderation is refused, {@code false} otherwise
     * @throws XWikiException in case of problems
     */
    public boolean refuseModeration(DocumentReference document, String reason) throws XWikiException;

    /**
     * moderating -> validating. + moderator looses edit rights
     * 
     * @param document the document reference to be validated
     * @return {@code true} if the document is submitted for validation, {@code false} otherwise
     * @throws XWikiException in case of problems
     */
    public boolean submitForValidation(DocumentReference document) throws XWikiException;

    /**
     * validating -> draft. + contributor and moderator get back rights
     * 
     * @param documnet the document to be validated
     * @param reason the reason for not validating
     * @return {@code true} if the validation is refused, {@code false} otherwise
     * @throws XWikiException in case of problems
     */
    public boolean refuseValidation(DocumentReference documnet, String reason) throws XWikiException;

    /**
     * validating -> validated. Rights stay the same as in validating state. This extra state is needed in order to be
     * able to delay the effective publishing of the document (making it available to users as a published document).
     * 
     * @param document to be validated
     * @return {@code true} if the document is validated, {@code false} otherwise
     * @throws XWikiException in case of problems
     */
    public boolean validate(DocumentReference document) throws XWikiException;

    /**
     * validated or validating -> published + document gets copied in its final place where it will be readonly anyway
     * 
     * @param document to be published
     * @return {@code true} if the document is published, {@code false} otherwise
     * @throws XWikiException in case of problems
     */
    public DocumentReference publish(DocumentReference document) throws XWikiException;

    /**
     * published -> draft. Published document gets deleted. Content from the published document can be copied to the
     * draft or just ignored, depending on the parameter.
     * 
     * @param document the published document that should be turned to draft.
     * @param forceToDraft the flag to specify if the published document cen be  copied to the draft
     * @return the draft document
     * @throws XWikiException in case of problems
     */
    public DocumentReference unpublish(DocumentReference document, boolean forceToDraft) throws XWikiException;

    /**
     * To edit back a draft whose status is published
     * 
     * @param document The draft that we want to edit
     * @return {@code true} if the draft should be edited, {@code false} otherwise
     * @throws XWikiException in case of problems 
     */
    public boolean editDraft(DocumentReference document) throws XWikiException;

    /**
     * published -> archived. Not yet sure how it would work.
     * 
     * @param document the published document that should be archived.
     * @return {@code true} if the document should be archived, {@code false} otherwise
     * @throws XWikiException in case of problems
     */
    public boolean archive(DocumentReference document) throws XWikiException;

    /**
     * archived -> draft. Archived document gets deleted. Content from the archived document can be copied to the draft
     * or just ignored, depending on the parameter.
     * 
     * @param document the archived document that should get drafted.
     * @return {@code true} if the document should be unarchived, {@code false} otherwise
     * @throws XWikiException in case of problems
     */
    public DocumentReference unarchive(DocumentReference document, boolean forceToDraft) throws XWikiException;

    /**
     * archived -> published. Not yet sure how it would work.
     * 
     * @param document the archived document that should go back to being published.
     * @return {@code true} if the document should be published fro archive, {@code false} otherwise
     * @throws XWikiException in case of problems
     */
    public boolean publishFromArchive(DocumentReference document) throws XWikiException;
}
