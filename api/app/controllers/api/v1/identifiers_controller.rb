class Api::V1::IdentifiersController < Api::V1::BaseController
  skip_authorization_check
  before_filter :authenticate_user!, :except => [:index, :create]

  # GET /identifiers
  # GET /identifiers.json
  def index
    @identifiers = Identifier.all

    render json: @identifiers
  end

  # GET /identifiers/1
  # GET /identifiers/1.json
  def show
    @identifier = Identifier.find(params[:id])

    render json: @identifier
  end

  # POST /identifiers
  # POST /identifiers.json
  def create
    @identifier = Identifier.new(params[:identifier])

    if @identifier.save
      # render json: @identifier, status: :created, location: @identifier
      render json: @identifier, status: :created
    else
      render json: @identifier.errors, status: :unprocessable_entity
    end
  end

  # PATCH/PUT /identifiers/1
  # PATCH/PUT /identifiers/1.json
  def update
    @identifier = Identifier.find(params[:id])

    if @identifier.update(params[:identifier])
      head :no_content
    else
      render json: @identifier.errors, status: :unprocessable_entity
    end
  end

  # DELETE /identifiers/1
  # DELETE /identifiers/1.json
  def destroy
    @identifier = Identifier.find(params[:id])
    @identifier.destroy

    head :no_content
  end
end
