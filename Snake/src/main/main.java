package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import snakeGame.Snake;

public class main extends Application{

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		Point2D screen_dimensions = new Point2D(600, 600);
		Group root = new Group();
        Scene scene = new Scene(root);
        
        GridPane pane = new GridPane();
       
        final Canvas game_area = new Canvas(screen_dimensions.getX(), screen_dimensions.getY());
        pane.add(game_area, 0, 0); 
        
        final GridPane button_pane = new GridPane();
        
        GridPane auto_b_pane = new GridPane();
        
        final ToggleGroup playSetting = new ToggleGroup();
        final RadioButton manual = new RadioButton("Manual");
        final RadioButton auto_safe = new RadioButton("Auto");
        final RadioButton auto_short = new RadioButton("Auto w/ shortcuts");
        manual.setToggleGroup(playSetting);
        auto_safe.setToggleGroup(playSetting);
        auto_short.setToggleGroup(playSetting);
        
        auto_b_pane.add(manual, 0, 0);
        auto_b_pane.add(auto_safe, 0, 1);
        auto_b_pane.add(auto_short, 0, 2);
        
        button_pane.add(auto_b_pane, 0, 0);
        
        final CheckBox debugButton = new CheckBox("Debug view");

        button_pane.add(debugButton, 1, 0);
        
        final Text length = new Text("Length:") ;
        button_pane.add(length, 1, 1);	
        
        final Text frame_time_title = new Text("Frame Time: ");
        final Slider frame_time = new Slider();
        
        frame_time.setMin(1);
        frame_time.setMax(1000);
        frame_time.setShowTickLabels(true);
        frame_time.setShowTickMarks(true);
        frame_time.setMajorTickUnit(50);
        frame_time.setMinorTickCount(5);
        
        button_pane.add(frame_time, 2, 0);
        button_pane.add(frame_time_title, 2, 1);
        
        final CheckBox pause = new CheckBox("Pause");
        button_pane.add(pause, 3, 0);
        
        final Text order_title = new Text("Order: ");
        final Slider order = new Slider();
        
        order.setMin(0);
        order.setMax(7);
        order.setShowTickMarks(true);
        order.setMajorTickUnit(1);
        order.setMinorTickCount(0);
        order.setSnapToTicks(true);
       
        button_pane.add(order, 4, 0);
        button_pane.add(order_title, 4, 1);
        
        final Button generate = new Button("Generate");
        button_pane.add(generate, 4, 2);
        
        pane.add(button_pane, 0, 1);
        
        root.getChildren().add(pane);
        final BoardGameService service = new BoardGameService(game_area, 3);
        
        final AnimationTimer l_timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
            	length.setText("Length: "+ service.getLength() + "\nFrames:"+ service.getFrames());
                }
        };
        l_timer.start();
           
        playSetting.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

			public void changed(ObservableValue<? extends Toggle> arg0, Toggle arg1, Toggle arg2) {
				if (playSetting.getSelectedToggle().equals(manual)){
					service.setAuto(0);
				}
				else if (playSetting.getSelectedToggle().equals(auto_safe)){
					service.setAuto(1);
				}
				else if (playSetting.getSelectedToggle().equals(auto_short)){
					service.setAuto(2);
				}
				
			}
        	
        });
       
        debugButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				service.setDebug(debugButton.isSelected());
			}
        });
        frame_time.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				service.setFrameTime(newValue.longValue());
				frame_time_title.setText(String.format("Frame Time: %.2fms", newValue.doubleValue()));
			}
        	
        });
        pause.setOnAction(new EventHandler<ActionEvent>(){
        	public void handle(ActionEvent event) {
        		if (service.getPause()) {
    				service.unpause();
    			}
    			else {
    				service.pause();
    			}
        	}
        });
        order.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				order_title.setText("Order: " + newValue.intValue());
			}
        	
        });
        generate.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent arg0) {
				service.generate((int) order.getValue());
				if(service.getAuto() == 0) {
					manual.setSelected(true);
			    } else if (service.getAuto() == 1) {
			      	auto_safe.setSelected(true);
			    }
			    else if (service.getAuto() == 2) {
			    	auto_short.setSelected(true);
			    }
			    pause.setSelected(service.getPause());
			    debugButton.setSelected(service.getDebug());
			    frame_time.adjustValue(service.getFrameTime());
			}
        });
        
        order.adjustValue(3);
        generate.fire();
        
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
        	public void handle(final KeyEvent keyEvent) {
        		if(keyEvent.getCode() == KeyCode.P) {
        			pause.fire();
        		}
        		service.Controls(keyEvent.getCode());
        		keyEvent.consume();
        	 }
        });
        
        primaryStage.setScene(scene);
      
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent event) {
				service.stop();
				l_timer.stop();
			}
        	
        });
        primaryStage.show();
        service.start();
	}
	
	private void resetButtons() {
		
	}

	
	private class BoardGameService{
		private Lock c_lock;
		private Snake game;
		long f_time;
		boolean cont;
		boolean pause;
		Condition p;
		AnimationTimer r_timer;
		Canvas game_area;
		private int frame_counter;
		
		public BoardGameService(Canvas game_area, int order) {
			f_time = 30;
			c_lock = new ReentrantLock();
			p = c_lock.newCondition();
			this.game_area = game_area;
			r_timer = new AnimationTimer() {
	            @Override
	            public void handle(long now) {
	            	c_lock.lock();
					game.render();
					c_lock.unlock();
	            }
	        };
			generate(order);
		}
		
		public void generate(int order) {
			c_lock.lock();
			game = new Snake(order);
			frame_counter = 0;
			setScreen(this.game_area);
			pause();
			c_lock.unlock();
		}
		public void Controls(KeyCode in) {
			c_lock.lock();
			game.controlHeading(in);
			c_lock.unlock();
		}
		public int getAuto() {
			c_lock.lock();
			int out = game.getAuto();
			c_lock.unlock();
			return out;
		}
		public void setAuto(int in) {
			c_lock.lock();
			game.setAuto(in);
			c_lock.unlock();
		}
		public boolean getDebug() {
			c_lock.lock();
			boolean out = game.getDebug();
			c_lock.unlock();
			return out;
		}
		public void setDebug(boolean in) {
			c_lock.lock();
			game.setDebug(in);
			c_lock.unlock();
		}
		public long getFrameTime() {
			c_lock.lock();
			long out = f_time;
			c_lock.unlock();
			return out;
		}
		public void setFrameTime(long milliseconds) {
			c_lock.lock();
			f_time = milliseconds;
			c_lock.unlock();
		}
		public int getLength() {
			c_lock.lock();
			int out = game.getLength();
			c_lock.unlock();
			return out;
		}
		
		public void setScreen(Canvas g) {
			c_lock.lock();
			System.out.println("set screen");
			game.setScreen(g);
			c_lock.unlock();
		}
		
		public void start() {
			cont = true;
			new Thread(new Gameloop(), "Game Thread").start();
			r_timer.start();
		}
		public void stop() {
			c_lock.lock();
			cont = false;
			pause = false;
			p.signal();
			c_lock.unlock();
			r_timer.stop();
		}
		public void pause() {
			c_lock.lock();
			pause = true;
			c_lock.unlock();
		}
		public void unpause() {
			c_lock.lock();
			if (cont == false) {
				start();
			}
			pause = false;
			p.signal();
			c_lock.unlock();
		}
		public boolean getPause() {
			c_lock.lock();
			boolean out = pause;
			c_lock.unlock();
			return out;
		}
		public int getFrames() {
			c_lock.lock();
			int out = frame_counter;
			c_lock.unlock();
			return out;
		}
		class Gameloop implements Runnable {
				Gameloop (){}
				
				public void run() {
					long s_time;
					long e_time;
					long sleep;
					try {
						while(cont) {
							c_lock.lock();
							while(pause) {
								p.await();
							}
							frame_counter++;
							s_time = System.nanoTime();
							if(cont) {
								cont = game.iterateGame();
							}
							c_lock.unlock();
							e_time = System.nanoTime();
							sleep = f_time - ((e_time-s_time)/ 1000000);
							if (sleep > 0) {
								Thread.sleep(sleep);
							}
						}
						game.render();
						System.out.println("Game over!");
					} catch (InterruptedException e) {
						System.out.println(e);
					}
					
				}
		}
	}
}