package com.olyware.mathlock.views;

public enum JoystickSelect {
	Vibrate, A, B, C, D, Question, Friends, Store, Progress, QuizMode, Settings, Missed, QuickUnlock, AddApp, SelectApp, DeleteApp, Share, Touch, Exit, ReturnToDefault, SelectLock;
	public static JoystickSelect fromValue(int s) {
		switch (s) {
		case -1:
			return Vibrate;
		case 0:
			return A;
		case 1:
			return B;
		case 2:
			return C;
		case 3:
			return D;
		case 4:
			return Question;
		case 5:
			return Friends;
		case 6:
			return Store;
		case 7:
			return Progress;
		case 8:
			return QuizMode;
		case 9:
			return Settings;
		case 10:
			return Missed;
		case 11:
			return QuickUnlock;
		case 12:
			return AddApp;
		case 13:
			return SelectApp;
		case 14:
			return DeleteApp;
		case 15:
			return Share;
		case 16:
			return Touch;
		case 17:
			return Exit;
		case 18:
			return ReturnToDefault;
		case 19:
			return SelectLock;
		default:
			return Vibrate;
		}
	}

	public static int fromValue(JoystickSelect s) {
		switch (s) {
		case Vibrate:
			return -1;
		case A:
			return 0;
		case B:
			return 1;
		case C:
			return 2;
		case D:
			return 3;
		case Question:
			return 4;
		case Friends:
			return 5;
		case Store:
			return 6;
		case Progress:
			return 7;
		case QuizMode:
			return 8;
		case Settings:
			return 9;
		case Missed:
			return 10;
		case QuickUnlock:
			return 11;
		case AddApp:
			return 12;
		case SelectApp:
			return 13;
		case DeleteApp:
			return 14;
		case Share:
			return 15;
		case Touch:
			return 16;
		case Exit:
			return 17;
		case ReturnToDefault:
			return 18;
		case SelectLock:
			return 19;
		default:
			return -1;
		}
	}
}
