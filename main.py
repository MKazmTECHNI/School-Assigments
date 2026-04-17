import importlib # peak libary btw
import os
import random
import sys
from pathlib import Path

# Wycisza techniczne ostrzeżenia FFmpeg backendu Qt6 typu:
# "Could not update timestamps for skipped samples"
os.environ.setdefault("QT_LOGGING_RULES", "qt.multimedia.ffmpeg=false")

from PyQt6.QtCore import QLoggingCategory, Qt, QUrl
from PyQt6.QtMultimedia import QAudioOutput, QMediaPlayer
from PyQt6.QtWidgets import (
    QApplication,
    QFrame,
    QGridLayout,
    QHBoxLayout,
    QLabel,
    QMessageBox,
    QPushButton,
    QSlider,
    QVBoxLayout,
    QWidget,
)

QLoggingCategory.setFilterRules("qt.multimedia.ffmpeg=false")


def format_time(milliseconds: int) -> str:
    total_seconds = max(0, milliseconds // 1000)
    minutes = total_seconds // 60
    seconds = total_seconds % 60
    return f"{minutes:02d}:{seconds:02d}"


class MusicPlayer(QWidget):
    def __init__(self) -> None: # a to ci fajny innit, króciuteńki
        super().__init__()
        self.setObjectName("MainWindow") # klasa głównego okna
        self.setWindowTitle("Mini MP3 Player")
        self.setFixedSize(350, 120) # FIXED resolution, im not sure if to stay with it tho i dont like it
        self.project_dir = Path(__file__).resolve().parent
        self.apply_stylesheet() # link stylesheet (im too used to css alr)

        self.playlist = sorted(self.project_dir.glob("*.mp3"))
        self.current_index = 0 # ten index lewo od nazwy cuz goal
        self.duration_ms = 0 # dlugosc piosenki
        self.position_ms = 0 # w której ms piosenki jestesmy

    # Actually playing sounds
        self.player = QMediaPlayer(self)
        self.audio_output = QAudioOutput(self)
        self.player.setAudioOutput(self.audio_output)
        self.audio_output.setVolume(0.3) 
        self.player.positionChanged.connect(self.on_position_changed)
        self.player.durationChanged.connect(self.on_duration_changed)
        self.player.mediaStatusChanged.connect(self.on_media_status_changed)
        self.player.errorOccurred.connect(self.on_player_error)

# Budowa elementów struktury
        self.time_label = QLabel("00:00")
        self.time_label.setObjectName("TimeDisplay")
        self.song_label = QLabel("01 BRAK UTWORÓW MP3")
        self.song_label.setFixedWidth(164)

        self.bitrate_value_label = QLabel("--")
        self.bitrate_value_label.setFixedWidth(32)
        self.mixrate_value_label = QLabel("--")
        self.mixrate_value_label.setFixedWidth(32)

    # tego nie ma w goalu od adriana so actual insertion is commented
        self.volume_value_label = QLabel("30")
        self.volume_value_label.setFixedWidth(40)
    # -----------------
        for label in [self.song_label, self.bitrate_value_label, self.mixrate_value_label, self.volume_value_label]: # shorter = cuter = better
            label.setObjectName("MetaValue")

        self.volume_slider = QSlider(Qt.Orientation.Horizontal)
        self.volume_slider.setRange(0, 100)
        self.volume_slider.setValue(30)
        self.volume_slider.valueChanged.connect(self.on_volume_changed)

        self.prev_btn = QPushButton("◄◄")
        self.play_btn = QPushButton("►")
        self.pause_btn = QPushButton("▮▮")
        # self.restart_btn = QPushButton("|◄")
        self.restart_btn = QPushButton("■")
        self.restart_btn.setStyleSheet("font-size: 14px;")
        self.next_btn = QPushButton("►►")
        for btn in [self.prev_btn, self.play_btn, self.pause_btn, self.restart_btn, self.next_btn]:
            btn.setObjectName("ControlButton")

        self.shuffle_btn = QPushButton("SHUFFLE")
        self.loop_btn = QPushButton("LOOP")
        for btn in [self.shuffle_btn, self.loop_btn]:
            btn.setCheckable(True)
            btn.setObjectName("ToggleButton")

        self.shuffle_btn.toggled.connect(lambda checked: self.loop_btn.setChecked(False) if checked else None)
        self.loop_btn.toggled.connect(lambda checked: self.shuffle_btn.setChecked(False) if checked else None)

        self.prev_btn.clicked.connect(self.play_previous)
        self.next_btn.clicked.connect(self.play_next)
        self.play_btn.clicked.connect(self.play_current)
        self.pause_btn.clicked.connect(self.player.pause)
        self.restart_btn.clicked.connect(self.restart_current)

        self.build_layout()

        if self.playlist:
            self.load_song(self.current_index)
        else:
            self.disable_controls_without_tracks()

    def apply_stylesheet(self) -> None: # .qss = .css but for Qt
        style_path = self.project_dir / "style.qss"
        try:
            self.setStyleSheet(style_path.read_text(encoding="utf-8"))
        except Exception:
            print("Probably should throw an error, but I'm cooked, and it's visible either")
            self.setStyleSheet("QWidget#MainWindow { background: #242424; }")
            QMessageBox.critical(self, "Error", "Failed to apply stylesheet")
            # raise Exception("Failed to apply stylesheet") # prolly should uncomment it

    def build_layout(self) -> None:
        display_panel = QFrame()
        display_panel.setObjectName("DisplayPanel")
        top_grid = QGridLayout(display_panel)
        top_grid.setContentsMargins(5, 4, 5, 4)
        top_grid.setHorizontalSpacing(4)
        top_grid.setVerticalSpacing(7) # more readable imo

        song_caption = QLabel("SONG")
        bitrate_caption = QLabel("BITRATE")
        mixrate_caption = QLabel("MIXRATE")
        stereo_top_label = QLabel("stereo")
        mono_label = QLabel("mono")
        volume_caption = QLabel("VOLUME")
        stereo_bottom_label = QLabel("stereo")
        kbps_label = QLabel("kbps")
        khz_label = QLabel("kHz")
        for label in [bitrate_caption, mixrate_caption, song_caption, mono_label, volume_caption]:
            label.setObjectName("MetaCaption")
        for label in [kbps_label, khz_label]:
            label.setObjectName("MetaUnit")
        for label in [stereo_top_label, stereo_bottom_label]:
            label.setObjectName("StereoOn")

    # Big container top (time, info(, volume (should be here)))
        top_grid.addWidget(self.time_label, 0, 0, 2, 1)
        top_grid.addWidget(song_caption, 0, 1)
        top_grid.addWidget(self.song_label, 0, 2, 1, 4)
        top_grid.addWidget(stereo_top_label, 0, 6, alignment=Qt.AlignmentFlag.AlignRight)

        top_grid.addWidget(bitrate_caption, 1, 1)
        top_grid.addWidget(self.bitrate_value_label, 1, 2)
        top_grid.addWidget(kbps_label, 1, 3)
        top_grid.addWidget(mixrate_caption, 1, 4)
        top_grid.addWidget(self.mixrate_value_label, 1, 5)
        top_grid.addWidget(khz_label, 1, 6)

        top_grid.addWidget(volume_caption, 2, 1)
        top_grid.addWidget(self.volume_slider, 2, 2, 1, 3)
        top_grid.addWidget(mono_label, 2, 5, alignment=Qt.AlignmentFlag.AlignRight)
        top_grid.addWidget(stereo_bottom_label, 2, 6, alignment=Qt.AlignmentFlag.AlignRight)

    # container controls layout
        controls_layout = QHBoxLayout()
        controls_layout.setContentsMargins(0, 0, 0, 0)
        controls_layout.setSpacing(3)
        controls_layout.addWidget(self.prev_btn)
        controls_layout.addWidget(self.play_btn)
        controls_layout.addWidget(self.pause_btn)
        controls_layout.addWidget(self.restart_btn)
        controls_layout.addWidget(self.next_btn)
        controls_layout.addSpacing(7)
        controls_layout.addWidget(self.shuffle_btn)
        controls_layout.addWidget(self.loop_btn)
        controls_layout.addStretch(1)

    # Big container bottom (controls, shuffle/loop, logo (currently also volume))
        bottom_panel = QFrame()
        bottom_panel.setObjectName("BottomPanel") # why the hell it here (main window class)
        bottom_panel_layout = QVBoxLayout(bottom_panel)
        bottom_panel_layout.setContentsMargins(5, 4, 5, 4)
        bottom_panel_layout.setSpacing(3)
        # bottom_panel_layout.addLayout(volume_layout)
        bottom_panel_layout.addLayout(controls_layout)

    # Main Layout (top_panel, bottom_panel)
        main_layout = QVBoxLayout() 
        main_layout.setContentsMargins(5, 5, 5, 5)
        main_layout.setSpacing(4)
        main_layout.addWidget(display_panel)
        main_layout.addWidget(bottom_panel)
        self.setLayout(main_layout)

    def disable_controls_without_tracks(self) -> None:
        for button in [
            self.prev_btn,
            self.play_btn,
            self.pause_btn,
            self.restart_btn,
            self.next_btn,
            self.shuffle_btn,
            self.loop_btn,
        ]:
            button.setEnabled(False)
        QMessageBox.information(
            self,
            "Brak plików MP3",
            "Nie znaleziono żadnych plików .mp3 w folderze projektu.",
        )

    def load_song(self, index: int) -> None:
        if not self.playlist:
            return

        self.current_index = index % len(self.playlist)
        song_path = self.playlist[self.current_index]
        self.song_label.setText(f"{self.current_index + 1:02d} {song_path.stem.upper()}")
        self.update_audio_info(song_path)

        self.player.setSource(QUrl.fromLocalFile(str(song_path)))
        self.player.setPosition(0)
        self.position_ms = 0
        self.refresh_time_label()

    def update_audio_info(self, song_path: Path) -> None:
        bitrate_value = "--"
        mixrate_value = "--"

        try:
            mp3_module = importlib.import_module("mutagen.mp3")
            mp3_class = getattr(mp3_module, "MP3")
            meta = mp3_class(song_path)
            if getattr(meta, "info", None):
                bitrate_kbps = int(meta.info.bitrate / 1000)
                mixrate_khz = float(meta.info.sample_rate) / 1000.0
                bitrate_value = str(bitrate_kbps)
                mixrate_value = f"{mixrate_khz:.1f}"
        except Exception:
            pass

        self.bitrate_value_label.setText(bitrate_value)
        self.mixrate_value_label.setText(mixrate_value)

    def play_current(self) -> None:
        if not self.playlist:
            return
        self.player.play()

    def restart_current(self) -> None:
        if not self.playlist:
            return
        self.player.setPosition(0)
        self.player.play()

    def play_previous(self) -> None:
        if not self.playlist:
            return
        self.current_index = (self.current_index - 1) % len(self.playlist)
        self.load_song(self.current_index)
        self.player.play()

    def play_next(self) -> None:
        if not self.playlist:
            return

        if self.shuffle_btn.isChecked() and len(self.playlist) > 1:
            next_index = self.current_index
            while next_index == self.current_index:
                next_index = random.randint(0, len(self.playlist) - 1)
            self.current_index = next_index
        else:
            self.current_index = (self.current_index + 1) % len(self.playlist)

        self.load_song(self.current_index)
        self.player.play()

    def on_volume_changed(self, value: int) -> None:
        self.audio_output.setVolume(value / 100)
        self.volume_value_label.setText(str(value))

    def on_position_changed(self, position: int) -> None:
        self.position_ms = position
        self.refresh_time_label()

    def on_duration_changed(self, duration: int) -> None:
        self.duration_ms = duration
        self.refresh_time_label()

    def refresh_time_label(self) -> None:
        current_text = format_time(self.position_ms)
        self.time_label.setText("► "+current_text)

    def on_media_status_changed(self, status: int) -> None:
        if status == QMediaPlayer.MediaStatus.EndOfMedia:
            if self.loop_btn.isChecked():
                self.restart_current()
            else:
                self.play_next()

    def on_player_error(self, error: QMediaPlayer.Error, _message: str = "") -> None:
        if error == QMediaPlayer.Error.NoError:
            return
        message = self.player.errorString() or "Nieznany błąd odtwarzania"
        QMessageBox.warning(
            self,
            "Błąd odtwarzania MP3",
            f"Nie można odtworzyć utworu: {self.playlist[self.current_index].name}\n\n{message}",
        )


if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = MusicPlayer()
    window.show()
    sys.exit(app.exec())