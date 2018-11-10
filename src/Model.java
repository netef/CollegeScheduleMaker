
import java.util.ArrayList;
import java.util.Collection;

import exceptions.CourseCodeAlreadyExistException;
import exceptions.CourseNameAlreadyExistException;
import exceptions.EndingTimeBeforeStartingTimeException;
import exceptions.RoomFullException;
import exceptions.TeacherTeachingException;
import exceptions.courseNotExistException;
import javafx.event.EventHandler;

public class Model implements IModel {

	private ArrayList<EventHandler<MyActionEvent>> listeners = new ArrayList<>();
	private AllCourses allCourses = new AllCourses();
	private Schedule schedule = new Schedule();

	// TODO FOR TESTING ONLY!!!
	@Override
	public Model getModelForTestingOnly() {
		return this;
	}

	// TODO FOR TESTING ONLY!!!
	public AllCourses getAllCoursesForTestingOnly() {
		return allCourses;
	}

	// TODO FOR TESTING ONLY!!!
	public Schedule getScheduleForTestingOnly() {
		return schedule;
	}

	private int ivokingSlotNumber;
	private ISlot[] inokedSlots;
	private boolean createAnotherShow;

	public int setShowNumberPlusOne(int courseCode) {
		int showNumber = 0;
		try {
			showNumber = allCourses.getCourseById(courseCode).getShows().size();
		} catch (courseNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return showNumber;
	}

	@Override
	public void createNewCourse(String[] courseInput) {
		try {
			allCourses.addCourse(toIntFromString(courseInput[0]), courseInput[1]);
			invokeListeners(Controller.DONE_CREATE_COURSE_MODEL);
			return;
		} catch (CourseCodeAlreadyExistException | NumberFormatException e) {
			invokeListeners(Controller.COURSE_CODE_ALREADY_EXIST_ERROR);
			return;
		} catch (CourseNameAlreadyExistException e) {
			invokeListeners(Controller.COURSE_NAME_ALREADY_EXIST_ERROR);
			return;
		}
	}

	@Override
	public int getIvokingSlotNumber() {
		return ivokingSlotNumber;
	}

	private void setIvokingSlotNumber(int ivokingSlotNumber) {
		this.ivokingSlotNumber = ivokingSlotNumber;
	}

	@Override
	public void registerListener(EventHandler<MyActionEvent> e) {
		listeners.add(e);

	}

	@Override
	public void createNewShow(int courseCode, String[][] slotsInput) {
		try {

			int showNumber = setShowNumberPlusOne(courseCode);
			// avoiding create same show after return from error input
			if (allCourses.getCourseById(courseCode).getShowByShowCourse(showNumber) == null) {// IS
																								// the
																								// show
																								// already
																								// created
																								// ?
				allCourses.addShow(courseCode, showNumber);
			} else {
				allCourses.getCourseById(courseCode).getShowByShowCourse(showNumber).getSlots().clear();// override
																										// data
																										// from
																										// the
																										// slots
																										// input

			}
			for (int i = 0; i < slotsInput.length; i++) {
				IDay.Day day = IDay.dayByString(slotsInput[i][0]);
				int startingTime = toIntFromString(slotsInput[i][1]);
				int endingTime = toIntFromString(slotsInput[i][2]);
				int numberOfRoom = 0;
				try {
					numberOfRoom = toIntFromString(slotsInput[i][3]);
				} catch (NumberFormatException e) {
					setIvokingSlotNumber(i);
					invokeListeners(Controller.ROOM_INPUT_ISNT_INTEGER);
					return;
				}
				String nameOfLect = slotsInput[i][4];
				try {
					try {
						allCourses.addSlot(courseCode, showNumber, i, day, startingTime, endingTime, numberOfRoom,
								nameOfLect);
					} catch (RoomFullException e) {
						setIvokingSlotNumber(i);
						invokeListeners(Controller.ROOM_FULL_EROOR);
						return;
					} catch (TeacherTeachingException e) {
						setIvokingSlotNumber(i);
						invokeListeners(Controller.TEACHER_ALREADY_TEACHING_ERROR);
						return;
					}
				} catch (EndingTimeBeforeStartingTimeException e) {
					setIvokingSlotNumber(i);
					invokeListeners(Controller.TIMING_ERROR);
					return;
				}
			}
			if (createAnotherShow == false) {
				invokeListeners(Controller.DONE_CREATE_SLOTS_MODEL);
			} else {
				createAnotherShow = false;
				invokeListeners(Controller.CREATE_ANOTHER_SHOW_MODEL);
			}
			return;
		} catch (courseNotExistException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

	}

	static public int toIntFromString(String intAsString) throws NumberFormatException {
		return Integer.parseInt(intAsString);
	}

	private void invokeListeners(String command) {
		for (EventHandler<MyActionEvent> listener : listeners) {
			listener.handle(new MyActionEvent(this, command));
		}
	}

	@Override
	public ICourse[] getAllCoursesForViewer() {
		ICourse[] collection = new ICourse[allCourses.getMapOfCourse().size()];
		allCourses.getMapOfCourse().values().toArray(collection);
		return collection;
	}

	@Override
	public void addCourseToSchedule(ICourse wantedCourse) {// if you get here ,
															// its posiible to
															// add that coourse
		try {

			schedule.addCourseToSchedule(allCourses.getCourseById(wantedCourse.getCourseCode()),
					(int) wantedCourse.getShowCodes().toArray()[0]);
			inokedSlots = new ISlot[allCourses.getCourseById(wantedCourse.getCourseCode())
					.getShowByShowCourse((int) wantedCourse.getShowCodes().toArray()[0]).getSlots().size()];
			allCourses.getCourseById(wantedCourse.getCourseCode())
					.getShowByShowCourse((int) wantedCourse.getShowCodes().toArray()[0]).getSlots()
					.toArray(inokedSlots);
			invokeListeners(Controller.COURSE_ADDED_TO_SCHEDULE);
		} catch (courseNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public ISlot[] getInokedSlots() {
		return inokedSlots;
	}

	@Override
	public void removeCourseFromSchedule(ICourse wantedCourse) {
		try {
			inokedSlots = new ISlot[allCourses.getCourseById(wantedCourse.getCourseCode())
					.getShowByShowCourse((int) wantedCourse.getShowCodes().toArray()[0]).getSlots().size()];
			allCourses.getCourseById(wantedCourse.getCourseCode())
					.getShowByShowCourse((int) wantedCourse.getShowCodes().toArray()[0]).getSlots()
					.toArray(inokedSlots);
			schedule.removeCourseFromSchedule(allCourses.getCourseById(wantedCourse.getCourseCode()));

			invokeListeners(Controller.COURSE_REMOVED_FROM_SCHEDULE);
		} catch (courseNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public ArrayList<ICourse> getImpossibleCourses() {
		ArrayList<ICourse> impossibleCourses = new ArrayList<>();
		for (Course checkCourse : allCourses.getMapOfCourse().values()) {
			for (int i = 0; i < checkCourse.getShowCodes().size(); i++) {
			if (schedule.timeValidSlots(checkCourse, (int) checkCourse.getShowCodes().toArray()[i]) == false) {
				Course course=new Course(checkCourse.getCourseCode(),checkCourse.getCourseName());
				course.getShows().put(i, checkCourse.getShows().get(i));
				impossibleCourses.add(course);
			}
			}
		}
		return impossibleCourses;
	}

	@Override
	public void createAnotherShow(int creatingCourseCode, String[][] slotsInput) {
		createAnotherShow = true;
		createNewShow(creatingCourseCode, slotsInput);

	}

}
