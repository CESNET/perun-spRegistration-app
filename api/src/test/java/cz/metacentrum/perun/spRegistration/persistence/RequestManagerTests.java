package cz.metacentrum.perun.spRegistration.persistence;

import cz.metacentrum.perun.spRegistration.persistence.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.managers.impl.RequestManagerImpl;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:application-context-test.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RequestManagerTests {

	@Autowired
	private RequestManagerImpl requestManager;

	private PerunAttributeDefinition def1;
	private PerunAttributeDefinition def2;
	private PerunAttribute attr1;
	private PerunAttribute attr2;
	private Request req1;
	private Request req2;
	private RequestSignature approval1;
	private User fakeUser;

	@Before
	public void setUp() {
		def1 = new PerunAttributeDefinition(1L, "attr_1", "namespace1", "desc1",
				"java.lang.String", "attr1", true, false, "facility",
				"baseFriendlyName", "friendlyNameParameter");
		def2 = new PerunAttributeDefinition(2L, "attr_2", "namespace2", "desc2",
				"java.lang.Boolean", "attr2", true, false, "facility",
				"baseFriendlyName", "friendlyNameParameter");

		prepareFakeUser();
		prepareAttributes();
		prepareRequests();
		prepareApprovals();

		Long req1Id = requestManager.createRequest(req1);
		req1.setReqId(req1Id);

		Long req2Id = requestManager.createRequest(req2);
		req2.setReqId(req2Id);
	}

	private void prepareAttributes() {
		attr1 = new PerunAttribute(); // STRING ATTRIBUTE
		attr1.setDefinition(def1);
		attr1.setComment("test comment");
		attr1.setOldValue("old val");
		attr1.setValue("new val");
		attr1.setFullName(def1.getFullName());

		attr2 = new PerunAttribute(); // BOOLEAN ATTRIBUTE
		attr2.setDefinition(def2);
		attr2.setComment("test comment");
		attr2.setOldValue(false);
		attr2.setValue(true);
		attr2.setFullName(def2.getFullName());
	}

	private void prepareRequests() {
		req1 = new Request();
		req1.setAction(RequestAction.REGISTER_NEW_SP);
		req1.setStatus(RequestStatus.NEW);
		req1.setFacilityId(null);
		req1.setModifiedBy(1L);
		req1.setAttributes(Collections.singletonMap(attr1.getFullName(), attr1));
		req1.setReqUserId(1L);

		req2 = new Request();
		req2.setAction(RequestAction.UPDATE_FACILITY);
		req2.setStatus(RequestStatus.WFA);
		req2.setFacilityId(2L);
		req2.setModifiedBy(2L);
		req2.setAttributes(Collections.singletonMap(attr2.getFullName(), attr2));
		req2.setReqUserId(2L);
	}

	private void prepareApprovals() {
		LocalDateTime now = LocalDateTime.now();
		approval1 = new RequestSignature();
		approval1.setFacilityId(2L);
		approval1.setLink("/link");
		approval1.setHash("hash");
		approval1.setSignerId(fakeUser.getId());
		approval1.setSignerName(fakeUser.getFullName());
		approval1.setSignerEmail(fakeUser.getEmail());
		approval1.setValidUntil(now.plusDays(10));
	}

	private void prepareFakeUser() {
		fakeUser = new User(
				1L,
				"title_before",
				"Test",
				"Middle",
				"User",
				"title_after"
		);
		fakeUser.setEmail("testUser@somewhere.com");
	}

	@Test
	public void createRequest() {
		Request request = new Request();
		request.setAction(RequestAction.REGISTER_NEW_SP);
		request.setStatus(RequestStatus.NEW);
		request.setFacilityId(null);
		request.setModifiedBy(3L);
		request.setReqUserId(3L);

		Long requestId = requestManager.createRequest(request);
		assertNotNull("Creating request should generate an ID", requestId);
		request.setReqId(requestId);

		Request fetched = requestManager.getRequestByReqId(requestId);
		assertNotNull("Created request could not be fetched", fetched);
		assertEquals("Stored request is not the same", request, fetched);
	}

	@Test
	public void updateRequest() {
		req1.setAction(RequestAction.DELETE_FACILITY);
		req1.setStatus(RequestStatus.APPROVED);
		req1.setFacilityId(3L);

		boolean result = requestManager.updateRequest(req1);
		assertTrue("Updating should return true", result);

		Request fetched = requestManager.getRequestByReqId(req1.getReqId());
		assertNotNull("Updated request has not been fetched", fetched);
		assertEquals("Updated request is different", req1, fetched);
	}

	@Test
	public void deleteRequest() {
		requestManager.deleteRequest(req1.getReqId());
		List<Request> inDb = requestManager.getAllRequests();

		assertNotNull("No results fetched", inDb);
		assertEquals("Only one request should be found", 1, inDb.size());
		assertFalse("Request should not be in DB", inDb.contains(req1));
	}

	@Test
	public void getRequestById() {
		Request fetched = requestManager.getRequestByReqId(req1.getReqId());
		assertEquals("Fetched request is different than expected", req1, fetched);
	}

	@Test
	public void getAllRequests() {
		List<Request> allRequests = requestManager.getAllRequests();

		assertNotNull("No result has been fetched", allRequests);
		assertEquals("Result size should be 2", 2, allRequests.size());
		assertThat("Result does not contain expected items", allRequests, hasItems(req1, req2));
	}

	@Test
	public void getRequestsByUserId() {
		List<Request> fetched = requestManager.getAllRequestsByUserId(req1.getReqUserId());

		assertNotNull("Should find one request but null collection returned", fetched);
		assertTrue("Result set should contain at least one request", fetched.size() > 0);
		assertEquals("Result set should contain exactly one request", 1, fetched.size());
		assertEquals("Request should be equal", req1, fetched.get(0));
	}

	@Test
	public void getAllRequestsByStatus() {
		List<Request> fetched = requestManager.getAllRequestsByStatus(req1.getStatus());

		assertNotNull("Should find one request but null collection returned", fetched);
		assertTrue("Result set should contain at least one request", fetched.size() > 0);
		assertEquals("Result set should contain exactly one request", 1, fetched.size());
		assertEquals("Request should be equal", req1, fetched.get(0));
	}

	@Test
	public void getAllRequestsByAction() {
		List<Request> fetched = requestManager.getAllRequestsByAction(req1.getAction());

		assertNotNull("Should find one request but null collection returned", fetched);
		assertTrue("Result set should contain at least one request", fetched.size() > 0);
		assertEquals("Result set should contain exactly one request", 1, fetched.size());
		assertEquals("Request should be equal", req1, fetched.get(0));
	}

	@Test
	public void getRequestsByFacilityId() {
		List<Request> fetched = requestManager.getAllRequestsByFacilityId(req2.getFacilityId());

		assertNotNull("Should find one request but null collection returned", fetched);
		assertTrue("Result set should contain at least one request", fetched.size() > 0);
		assertEquals("Result set should contain exactly one request", 1, fetched.size());
		assertEquals("Request should be equal", req2, fetched.get(0));

	}

	@Test
	public void getAllRequestsByFacilityIds() {
		Set<Long> ids = new HashSet<>();
		ids.add(req2.getFacilityId());
		List<Request> fetched = requestManager.getAllRequestsByFacilityIds(ids);

		assertNotNull("Should find one request but null collection returned", fetched);
		assertTrue("Result set should contain at least one request", fetched.size() > 0);
		assertEquals("Result set should contain exactly two requests", 1, fetched.size());
		assertThat("Result does not contain expected items", fetched, hasItems(req2));
	}

	@Test
	public void storeApprovalLink() {
		boolean res = requestManager.storeApprovalLink(approval1.getSignerEmail(), approval1.getHash(),
				approval1.getFacilityId(), approval1.getLink(), approval1.getValidUntil());
		assertTrue("Storing link should return true", res);
	}

	@Test
	public void addSignature() {
		requestManager.storeApprovalLink(approval1.getSignerEmail(), approval1.getHash(),
				approval1.getFacilityId(), approval1.getLink(), approval1.getValidUntil());
		LocalDateTime now = LocalDateTime.now();

		boolean res = requestManager.addSignature(approval1.getFacilityId(), approval1.getHash(), fakeUser, now);
		approval1.setSignedAt(now);

		List<RequestSignature> found = requestManager.getRequestSignatures(approval1.getFacilityId());

		assertTrue("Storing signature should return true", res);
		assertNotNull("Found cannot be null", found);
		assertTrue("Should find at least one signature", found.size() > 0);
		assertEquals("Only one approval should be in DB", 1, found.size());
		assertEquals("Approvals are not the same", approval1, found.get(0));
	}

	@Test
	public void getRequestSignatures() {
		requestManager.storeApprovalLink(approval1.getSignerEmail(), approval1.getHash(),
				approval1.getFacilityId(), approval1.getLink(), approval1.getValidUntil());
		LocalDateTime now = LocalDateTime.now();
		requestManager.addSignature(approval1.getFacilityId(), approval1.getHash(), fakeUser, now);
		approval1.setSignedAt(now);

		List<RequestSignature> res = requestManager.getRequestSignatures(approval1.getFacilityId());

		assertNotNull("Result cannot be null", res);
		assertTrue("Should find at least one signature", res.size() > 0);
		assertEquals("Only one approval should be in DB", 1, res.size());
		assertEquals("Approvals are not the same", approval1, res.get(0));
	}

}
