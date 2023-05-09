package org.herac.tuxguitar.app.view.component.tab;

import org.herac.tuxguitar.app.document.TGDocument;
import org.herac.tuxguitar.app.document.TGDocumentListManager;
import org.herac.tuxguitar.graphics.control.TGLayout;
import org.herac.tuxguitar.graphics.control.TGTrackImpl;
import org.herac.tuxguitar.song.helpers.TGBeatRangeIterator;
import org.herac.tuxguitar.song.models.TGBeat;
import org.herac.tuxguitar.ui.resource.UIPainter;
import org.herac.tuxguitar.util.TGBeatRange;
import org.herac.tuxguitar.util.TGNoteRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by tubus on 20.12.16.
 */
public class Selector {

	private final Tablature tablature;
	private TGBeat initial;
	private TGBeat start;
	private TGBeat end;
	private boolean active;

	public Selector(Tablature tablature) {
	    this.tablature = tablature;
	}

	public void initializeSelection(TGBeat beat) {
		initial = beat;
		start = beat;
		end = beat;
		active = false;

		this.saveState();
	}

	public void updateSelection(TGBeat beat) {
	    if (initial == null || beat == null) {
	    	initializeSelection(beat);
		} else {
	    	if (beat != initial) {
	    		active = true;
			}
			if (initial.getMeasure().getNumber() < beat.getMeasure().getNumber() || initialIsEarlierInTheSameMeasure(beat)) {
				start = initial;
				end = beat;
			} else {
				start = beat;
				end = initial;
			}
			this.saveState();
		}
	}

	public void clearSelection() {
		initializeSelection(null);
	}

	private boolean initialIsEarlierInTheSameMeasure(TGBeat beat) {
		return initial.getMeasure().getNumber() == beat.getMeasure().getNumber() && initial.getStart() < beat.getStart();
	}

	public TGBeat getInitialBeat() {
		return initial;
	}

	public TGBeat getStartBeat() {
		return start;
	}

	public TGBeat getEndBeat() {
		return end;
	}

	public boolean isActive() {
		return active;
	}

	public void paintSelectedArea(TGLayout viewLayout, UIPainter painter) {
		if (isActive()) {
			TGTrackImpl track = (TGTrackImpl) initial.getMeasure().getTrack();
			track.paintBeatSelection(viewLayout, painter, start, end);
		}
	}

	public TGNoteRange getNoteRange(Collection<Integer> voices) {
		return new TGNoteRange(start, end, voices);
	}

	public TGBeatRange getBeatRange() {
		if (!isActive()) {
			return TGBeatRange.empty();
		}
		List<TGBeat> beats = new ArrayList<>();
		for (TGBeatRangeIterator it = new TGBeatRangeIterator(start, end); it.hasNext(); ) {
			beats.add(it.next());
		}
		return new TGBeatRange(beats);
	}

	private void saveState() {
		TGDocumentListManager documents = TGDocumentListManager.getInstance(this.tablature.getContext());
		TGDocument document = this.initial == null ? documents.findCurrentDocument() : documents.findDocument(this.initial.getMeasure().getTrack().getSong());
		document.setSelectionStart(this.getStartBeat());
		document.setSelectionEnd(this.getEndBeat());
	}

	public void restoreStateFrom(TGDocument document) {
		TGBeat start = document.getSelectionStart();
		TGBeat end = document.getSelectionEnd();
		this.initializeSelection(start);
		this.updateSelection(end);
	}
}
