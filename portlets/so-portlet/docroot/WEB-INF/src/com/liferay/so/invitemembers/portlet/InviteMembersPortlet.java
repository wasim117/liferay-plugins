/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This file is part of Liferay Social Office. Liferay Social Office is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Liferay Social Office is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Liferay Social Office. If not, see http://www.gnu.org/licenses/agpl-3.0.html.
 */

package com.liferay.so.invitemembers.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletURLFactoryUtil;
import com.liferay.so.service.MemberRequestLocalServiceUtil;
import com.liferay.so.util.PortletKeys;
import com.liferay.so.util.WebKeys;
import com.liferay.util.bridges.mvc.MVCPortlet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

/**
 * @author Ryan Park
 */
public class InviteMembersPortlet extends MVCPortlet {

	public void sendInvites(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		try {
			doSendInvite(actionRequest, actionResponse);
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e, e);
			}
		}
	}

	protected void doSendInvite(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		long groupId = ParamUtil.getLong(actionRequest, "groupId");
		long[] receiverUserIds = getLongArray(actionRequest, "receiverUserIds");
		String[] receiverEmailAddresses = getStringArray(
			actionRequest, "receiverEmailAddresses");
		long invitedRoleId = ParamUtil.getLong(actionRequest, "invitedRoleId");
		long invitedTeamId = ParamUtil.getLong(actionRequest, "invitedTeamId");

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		if (!UserLocalServiceUtil.hasGroupUser(
				groupId, themeDisplay.getUserId())) {

			return;
		}

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			actionRequest);

		PortletURL portletURL = PortletURLFactoryUtil.create(
			actionRequest, PortletKeys.SITE_REDIRECTOR, themeDisplay.getPlid(),
			PortletRequest.RENDER_PHASE);

		portletURL.setParameter("struts_action", "/my_sites/view");
		portletURL.setParameter("groupId", String.valueOf(groupId));

		Group group = GroupLocalServiceUtil.getGroup(groupId);

		portletURL.setParameter(
			"privateLayout", String.valueOf(!group.hasPublicLayouts()));

		portletURL.setWindowState(LiferayWindowState.NORMAL);

		String createAccountURL = PortalUtil.getCreateAccountURL(
			PortalUtil.getHttpServletRequest(actionRequest), themeDisplay);

		createAccountURL = HttpUtil.addParameter(
			createAccountURL, "redirect", portletURL.toString());

		serviceContext.setAttribute("createAccountURL", createAccountURL);

		String loginURL =
			themeDisplay.getPortalURL() + themeDisplay.getURLSignIn();

		loginURL = HttpUtil.addParameter(
			loginURL, "redirect", portletURL.toString());

		serviceContext.setAttribute("loginURL", loginURL);

		MemberRequestLocalServiceUtil.addMemberRequests(
			themeDisplay.getUserId(), groupId, receiverUserIds, invitedRoleId,
			invitedTeamId, serviceContext);

		MemberRequestLocalServiceUtil.addMemberRequests(
			themeDisplay.getUserId(), groupId, receiverEmailAddresses,
			invitedRoleId, invitedTeamId, serviceContext);
	}

	protected long[] getLongArray(PortletRequest portletRequest, String name) {
		String value = portletRequest.getParameter(name);

		if (value == null) {
			return null;
		}

		return StringUtil.split(GetterUtil.getString(value), 0L);
	}

	protected String[] getStringArray(
		PortletRequest portletRequest, String name) {

		String value = portletRequest.getParameter(name);

		if (value == null) {
			return null;
		}

		return StringUtil.split(GetterUtil.getString(value));
	}

	private static Log _log = LogFactoryUtil.getLog(InviteMembersPortlet.class);

}